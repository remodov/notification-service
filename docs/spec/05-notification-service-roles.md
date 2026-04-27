---
type: context-section
context: notification-service
parent: "[[notification-service]]"
section: roles
tier: A
tags:
  - roles
  - bc/notification
---

## 5. Роли и права

Notification — внутренний сервис, у него нет публичного API для покупателей. Все REST-эндпоинты делятся на две группы:

| Endpoint | Кому |
|---|---|
| `POST /webhooks/email-events` | SMTP-провайдер (фиксированный IP + HMAC-подпись в заголовке) |
| `GET /api/v1/notifications`, `POST /api/v1/notifications/{id}/retry` | оператор поддержки (роль `support-operator` в Keycloak) |

ABAC отсутствует — оператор видит **все** уведомления независимо от того, кому они отправлялись. Это сознательное ограничение Tier A: фильтрация по «своему направлению» (например, только заказы конкретного продавца) — это уровень Customer BFF / Seller BFF, не Notification.

Нет ролей `customer` или `seller`: пользователи не имеют прямого доступа к Notification — свой inbox видят через Customer BFF, который запрашивает у Notification нужные строки от имени пользователя (REST + service-to-service auth).
