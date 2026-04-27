---
type: context-section
context: notification-service
parent: "[[notification-service]]"
section: commands
tier: A
tags:
  - commands
  - bc/notification
---

## 7. Операции (Commands)

На Tier A — **просто список операций**, не `UseCase` записи.

### Внутренние (триггерятся событиями / шедулерами)

| Операция | Триггер | Что делает |
|---|---|---|
| `processOrderEvent(record)` | Kafka-консьюмер `marketplace.orders.v1` | Создаёт `Notification`-записи (по таблице каналов из BR-N2), отдаёт каждую в очередь отправки. Идемпотентен по `event_id`. |
| `dispatchPending()` | каждые 1s, шедулер | Берёт из БД `notifications` со статусом `QUEUED` (limit 100), отправляет через провайдера, обновляет статус. |
| `processEmailWebhook(payload)` | webhook от Mailgun | По `external_id` находит `notification`, переводит в `DELIVERED` или `BOUNCED`. |
| `purgeOldRecords()` | ежедневно 03:00 | Удаляет записи старше 90 дней (BR-N8). |

### Операторские (через REST)

| Операция | Эндпоинт | Что делает |
|---|---|---|
| Retry уведомления | `POST /api/v1/notifications/{id}/retry` | Только для `FAILED`. Сбрасывает `last_error`, ставит статус `QUEUED`, добавляет новую запись в `delivery_attempts`. |
| Пометить «безнадёжно» | `POST /api/v1/notifications/{id}/abandon` | Из `FAILED` уводит в финальный `FAILED` без возможности retry. Используется когда оператор убедился, что email невалидный. |

> На Tier B эти операции стали бы парами `RetryNotificationUseCase` / `AbandonNotificationUseCase`. На Tier A — методы `NotificationService`, ничего не теряем.
