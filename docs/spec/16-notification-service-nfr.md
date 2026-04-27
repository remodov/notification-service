---
type: context-section
context: notification-service
parent: "[[notification-service]]"
section: nfr
tier: A
tags:
  - nfr
  - bc/notification
---

## 16. Нефункциональные требования

### Производительность

- Пиковая нагрузка: 500 событий Order'а в секунду (по верхней границе планов маркетплейса) → до **1000 уведомлений** (email + push). Один инстанс держит, без шардирования.
- p95 latency «событие → email ушёл в Mailgun» — ≤ **5 секунд**.
- p99 — ≤ **30 секунд** (включая 1 retry при transient).

### Надёжность

- **At-least-once** доставка: лучше отправить дважды (получатель увидит дубль), чем не отправить вообще. Идемпотентность на уровне консьюмера (BR-N1) защищает от типичных дублей; редкие edge-кейсы вроде сбоя между «отправили» и «commit offset» допускаем — это менее критично, чем потерять подтверждение заказа.
- При сбое инстанса в момент `dispatchPending` — потерянные `QUEUED`-уведомления подберутся следующим запуском. Идемпотентности на провайдере нет, поэтому возможен дубль письма.

### Безопасность

- HMAC-валидация всех webhook'ов от Mailgun. Подпись проверяем по `X-Mailgun-Signature` + `X-Mailgun-Timestamp` (timestamp защищает от replay > 5 минут).
- PII (email, push-token) шифруется в БД на уровне Postgres TDE. В логах — маскируется (`u***@example.com`).
- 90-дневный TTL (BR-N8) — обязательный compliance-фактор, не «приятная фича».
- Service-to-service вызовы Customer BFF — через JWT с `service-account` ролью, не от имени пользователя.

### Наблюдаемость

- Метрики Prometheus: `notification_created_total{event_type, channel}`, `notification_status_total{status}`, `notification_provider_latency_seconds{provider}`, `notification_template_missing_total`, `notification_retry_total{reason}`.
- Алёрты:
  - `notification_failed_total > 5/min` — что-то системно сломалось.
  - `notification_template_missing_total > 0` — где-то пропустили шаблон, добавлен новый event_type.
  - `notification_provider_latency_seconds{p99} > 10s` — провайдер тормозит.
- Логи: `event_id`, `notification_id`, `external_id` пишутся в каждой строке для трейсинга. Tracing — OpenTelemetry, span-ы для consume → format → send.

### Эксплуатация

- Обновление шаблона — без рестарта (`UPDATE templates SET body = ... WHERE key = ...`). Кэшировать шаблоны нельзя или с TTL ≤ 60 секунд.
- Подключение нового провайдера — отдельный adapter. На запуске — Mailgun + FCM, прозрачно расширяется до SES + APNs.
- Локализация — поле `locale` у пользователя (берём в Customer BFF). На запуске два языка: `ru`, `en`. Шаблонов — 8 событий × 2 канала × 2 языка = 32 строки в `templates`.
