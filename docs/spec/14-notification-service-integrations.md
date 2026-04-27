---
type: context-section
context: notification-service
parent: "[[notification-service]]"
section: integrations
tier: A
tags:
  - integrations
  - bc/notification
---

## 14. Интеграции

### Inbound: Kafka

| Топик | Событие | Действие |
|---|---|---|
| `marketplace.orders.v1` | `OrderConfirmed` / `OrderPaid` / `OrderShipped` / `OrderDelivered` / `OrderCancelled` / `OrderRefunded` / `DisputeOpened` / `DisputeResolved` | создать `Notification` по таблице каналов |

Контракт события — определяется Order Service. Notification — **Conformist**: подстраивается под чужой контракт, никогда не диктует. Это нормально для Tier A: писать своё anti-corruption layer над schema из Order Service оверкилл, проще закладываться на стабильность контракта (semver).

### Inbound: REST (от SMTP-провайдера)

```
POST /webhooks/email-events
X-Mailgun-Signature: <hmac-sha256>
X-Mailgun-Timestamp: <unix-ts>
X-Mailgun-Token: <random>

{
  "event": "delivered" | "bounced" | "complained",
  "id": "abc-123",
  "message": { "headers": { "message-id": "..." } },
  "recipient": "user@example.com",
  "timestamp": 1700000000
}
```

HMAC-валидация заголовка обязательна (BR — `WEBHOOK_SIGNATURE_INVALID`). Дедупликация — по `(notification.external_id, webhook_event_id)`.

### Inbound: REST (от Admin UI)

| Метод | Путь | Кто | Что |
|---|---|---|---|
| `GET` | `/api/v1/notifications` | оператор | список с фильтрами |
| `GET` | `/api/v1/notifications/{id}` | оператор | детали |
| `POST` | `/api/v1/notifications/{id}/retry` | оператор | retry |
| `POST` | `/api/v1/notifications/{id}/abandon` | оператор | финализация без retry |

### Outbound: REST (Customer BFF)

```
GET /api/v1/users/{userId}/contact
→ {
  "userId": "...",
  "email": "user@example.com",
  "pushTokens": ["...fcm-token-1...", "...fcm-token-2..."],
  "locale": "ru"
}
```

Resilience: timeout 1s, retry 1, circuit breaker. При недоступности — уведомление помечается `FAILED` с `last_error="contact lookup unavailable"`, оператор может ретраить когда Customer BFF поднимется.

### Outbound: SMTP-провайдер

```
POST https://api.mailgun.net/v3/{domain}/messages
Authorization: Basic ...
Content-Type: multipart/form-data

from=<no-reply@marketplace.ru>
to=<user@example.com>
subject=Заказ ABC-123 подтверждён
html=<отрендеренное тело>
o:tracking=yes        ← включает webhook 'delivered'/'bounced'

→ 200 { "id": "<external_id>", "message": "Queued. Thank you." }
```

`external_id` сохраняется в `notification.external_id` для матчинга с webhook'ами.

### Outbound: FCM

```
POST https://fcm.googleapis.com/v1/projects/{project}/messages:send
Authorization: Bearer <oauth-token>

{ "message": { "token": "...", "notification": { ... } } }

→ 200
```

Без webhook — статус сразу `SENT`.
