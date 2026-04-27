---
type: context-section
context: notification-service
parent: "[[notification-service]]"
section: errors
tier: A
tags:
  - errors
  - bc/notification
---

## 13. Каталог ошибок

Notification — внутренний сервис, ошибки видит только оператор и интеграторы (Mailgun webhook, Customer BFF). Поэтому короткий каталог. Формат [RFC 9457 Problem Details](https://vikulin-va.ru/rest-api-style-guide/07-errors/).

| `code` | HTTP | Когда |
|---|---|---|
| `NOTIFICATION_NOT_FOUND` | 404 | retry / детали по несуществующему id |
| `INVALID_STATUS_FOR_RETRY` | 409 | retry на не-FAILED |
| `WEBHOOK_SIGNATURE_INVALID` | 401 | webhook от Mailgun без правильной HMAC-подписи |
| `TEMPLATE_MISSING` | — (внутренняя) | пишется в `notification.last_error` и уведомление не создаётся (BR-N3); внешне не видна |
| `CONTACT_LOOKUP_FAILED` | — (внутренняя) | Customer BFF недоступен или 5xx; уведомление получает `FAILED` после ретраев |
| `PROVIDER_UNAVAILABLE` | — (внутренняя) | Mailgun/FCM недоступен; уведомление получает `FAILED` после ретраев |

Внутренние ошибки наружу не светят — для интеграторов это либо 200 (webhook принят), либо 5xx (наша проблема).
