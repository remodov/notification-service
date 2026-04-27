---
type: context-section
context: notification-service
parent: "[[notification-service]]"
section: acceptance
tier: A
tags:
  - acceptance
  - bc/notification
---

## 15. Критерии приёмки

Минимально приёмочная функциональность для запуска.

| AC | Описание |
|---|---|
| AC-N1 | На каждое из восьми событий Order'а (см. BR-N2) создаются записи в правильных каналах. Тестами через embedded Kafka. |
| AC-N2 | Идемпотентный консьюмер: повторная доставка не создаёт второй `notification` (BR-N1). |
| AC-N3 | Mailgun-webhook `delivered` переводит `SENT` → `DELIVERED`. |
| AC-N4 | Mailgun-webhook `bounced` переводит `SENT` → `BOUNCED`. |
| AC-N5 | Невалидная HMAC-подпись webhook → 401. |
| AC-N6 | Транзиентная ошибка провайдера → 3 попытки, затем `FAILED`. |
| AC-N7 | Permanent error (4xx) — без ретраев, сразу `FAILED`. |
| AC-N8 | Шаблон отсутствует — уведомление не создаётся, метрика `notification_template_missing_total++`. |
| AC-N9 | `POST /retry` на FAILED — переводит в `QUEUED`, на не-FAILED — 409. |
| AC-N10 | Ночной purge удаляет записи старше 90 дней. |
| AC-N11 | Журнал доставки оператор видит с фильтрами и пагинацией. |
| AC-N12 | Customer BFF недоступен → уведомление в `FAILED`, retry оператора срабатывает после восстановления BFF. |
