---
type: context-section
context: notification-service
parent: "[[notification-service]]"
section: rules
tier: A
tags:
  - rules
  - bc/notification
---

## 6. Бизнес-правила

`BR-N1` **Идемпотентность по `event_id`.** Повторная доставка одного и того же сообщения из Kafka не создаёт дубль уведомления. Реализуется через `processed_events` — `INSERT ... ON CONFLICT DO NOTHING`; если конфликт — событие уже обработано.

`BR-N2` **Каналы выбираются жёстко по типу события.** Таблица в коде:

| Событие | EMAIL | PUSH |
|---|---|---|
| `OrderConfirmed` | ✅ | ✅ |
| `OrderPaid` | ✅ | ✅ |
| `OrderShipped` | ✅ (с трек-номером) | ✅ |
| `OrderDelivered` | ✅ (запрос отзыва) | — |
| `OrderCancelled` | ✅ | ✅ |
| `OrderRefunded` | ✅ | ✅ |
| `DisputeOpened` | — | ✅ продавцу + email оператору |
| `DisputeResolved` | ✅ | ✅ |

«Перевести правило в БД» (динамический выбор каналов через админку) — расширение Tier B.

`BR-N3` **Шаблон обязателен.** Если для пары `(event_type, channel, locale)` шаблон не найден — уведомление **не создаётся** (запись не появляется в `notifications`). Создаётся алерт в Prometheus (`notification_template_missing_total`). Это бизнес-инвариант: лучше пропустить уведомление, чем отправить пустое.

`BR-N4` **Контакт материализуется на момент отправки.** Email и push-токены, полученные из Customer BFF, **записываются** в `notifications.contact`. Если потом пользователь поменяет email, в журнале остаётся тот, на который реально ушло письмо.

`BR-N5` **Retry-политика.** 3 попытки с задержками 30s / 5min / 30min при `TRANSIENT_ERROR` (`5xx` от провайдера, network timeout). При `PERMANENT_ERROR` (`4xx`, например невалидный формат email) — сразу `FAILED`, без ретраев.

`BR-N6` **Webhook deduplication.** SMTP-провайдер может прислать одно и то же `delivered`-событие дважды. Дедупликация — по `(notification_id, webhook_event_id)`; повтор просто игнорируется.

`BR-N7` **PUSH без подтверждения доставки.** FCM не возвращает webhook о доставке. Уведомление, ушедшее в FCM с HTTP 200, помечается `SENT` и **никогда** не переходит в `DELIVERED`. Это явное ограничение, которое надо понимать оператору при чтении журнала.

`BR-N8` **Срок хранения журнала — 90 дней.** Старше 90 дней `notifications` и `delivery_attempts` удаляются ночным джобом. Юридическое обоснование: для прохождения GDPR/152-ФЗ нужно ограничить хранение логов с PII (email, push-токен).

`BR-N9` **Ограничение скорости отправки.** Не более 100 писем в секунду в одного провайдера. Реализуется как in-memory token bucket — для Tier A этого хватает (один инстанс на старте, потом — Redis-based лимитер при горизонтальном масштабировании).
