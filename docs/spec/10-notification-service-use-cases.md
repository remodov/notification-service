---
type: context-section
context: notification-service
parent: "[[notification-service]]"
section: use-cases
tier: A
tags:
  - use-cases
  - bc/notification
---

## 10. Use Cases

Главные сквозные сценарии. На Tier A это **истории**, не формальные UseCase-записи.

### UC-N1: Подтверждение заказа → email + push покупателю

1. Order Service публикует `OrderConfirmed` в `marketplace.orders.v1`.
2. Notification-консьюмер получает запись, проверяет `event_id` через `processed_events`.
3. Из payload вытаскивает `customerId`, обращается в Customer BFF: `GET /api/v1/users/{id}/contact` → `{email, pushTokens, locale}`.
4. По таблице каналов (BR-N2) для `OrderConfirmed` → `EMAIL` + `PUSH`. Создаёт две записи `notifications` со статусом `QUEUED`.
5. Шедулер `dispatchPending` забирает обе записи в очередь отправки.
6. EMAIL: рендерит шаблон `order.confirmed.email` (subject + body с `${order_id}`, `${customer_name}`), отправляет в Mailgun → `external_id`. Статус `SENT`.
7. PUSH: отправляет в FCM → 200. Статус `SENT` (без webhook).
8. Через ~30 секунд Mailgun присылает webhook `delivered` → статус EMAIL переходит в `DELIVERED`.

### UC-N2: Mailgun отвечает 503 → ретрай

1. Notification отправляет email, Mailgun отвечает `503`.
2. Создаётся запись в `delivery_attempts` с `result=TRANSIENT_ERROR`.
3. Через 30 секунд (BR-N5) шедулер делает повтор. Mailgun снова `503`.
4. Через 5 минут — третий повтор. Mailgun снова `503`.
5. Все три попытки исчерпаны → статус `FAILED`, `last_error="Mailgun 503 после 3 попыток"`. Алерт `notification_failed_total > 0` стреляет в Prometheus.
6. Оператор открывает админку, видит `FAILED`-уведомление, делает `POST /retry`. Статус → `QUEUED`, попадает в очередь, отправляется успешно.

### UC-N3: Адрес невалидный → `BOUNCED`

1. Notification отправляет email на `notexist@gmail.com`, Mailgun принимает (200) → статус `SENT`.
2. Через 2 минуты Mailgun присылает webhook `bounced` (категория `permanent`).
3. Notification переводит запись в `BOUNCED`.
4. Оператор видит в админке причину: «Mailgun: `permanent bounce — invalid recipient`».

### UC-N4: Ручной разбор оператора

1. Покупатель пишет в поддержку: «не пришёл код подтверждения».
2. Оператор открывает админку, фильтрует по `userId`, видит запись `notification` со статусом `BOUNCED`.
3. Кликает «детали» — видит `delivery_attempts`, ответ Mailgun: «adresat unreachable».
4. Сообщает покупателю в чате: «у вас невалидный email, обновите в профиле».
