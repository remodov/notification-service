---
type: context-section
context: notification-service
parent: "[[notification-service]]"
section: language
tier: A
tags:
  - language
  - bc/notification
---

## 2. Ubiquitous Language

Глоссарий очень короткий — Notification работает с минимальным числом понятий.

| Термин | Определение |
|---|---|
| **Notification** | Запись в журнале: одно событие, одна попытка доставки в один канал одному адресату. Если на одно событие отправляется письмо + push — это две `Notification`-записи. |
| **Channel** | Канал доставки. На запуске два: `EMAIL`, `PUSH`. Третий (`SMS`) — расширение без переписывания, добавится только провайдер. |
| **Recipient** | Кому отправляем. Хранится `userId` + материализованный контакт на момент отправки (email-адрес или push-токен) — нужно для журнала: пользователь мог поменять email, а в журнале должна остаться правда. |
| **Template** | Шаблон сообщения в БД: ключ (`order.confirmed`) + язык + субъект и тело с плейсхолдерами `${var}`. |
| **Source event** | Входящее событие из Kafka, по которому создана `Notification`. Сериализованный JSON хранится для дебага и retry. |
| **Provider** | Внешняя система доставки: Mailgun, FCM, в будущем SMS. У каждой `Notification` ровно один. |
| **Delivery status** | Статус: `QUEUED`, `SENT`, `DELIVERED`, `BOUNCED`, `FAILED`. См. раздел 4. |

### Намеренно НЕ используются

- **Aggregate / Entity / Value Object** — Notification это row в таблице, не агрегат.
- **Domain event / saga** — мы не публикуем события, не координируем процессы.
- **Customer / Seller** — для Notification они оба просто `userId`. Никакой дополнительной семантики не нужно.
