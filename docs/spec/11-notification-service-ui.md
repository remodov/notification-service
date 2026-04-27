---
type: context-section
context: notification-service
parent: "[[notification-service]]"
section: ui
tier: A
tags:
  - ui
  - bc/notification
---

## 11. UI-спецификация

Один интерфейс — админка для `support-operator`. Никаких пользовательских интерфейсов сам Notification не имеет (inbox показывает Customer BFF).

### Экран «Журнал уведомлений» (`/admin/notifications`)

| Элемент | Поведение |
|---|---|
| Фильтры (top bar) | `userId` (ввод), `status` (multi-select), `eventType` (multi-select), `channel` (toggle EMAIL/PUSH), диапазон дат |
| Таблица | колонки: `Дата`, `Получатель (email/userId)`, `Канал`, `Тип события`, `Статус` (цветной badge), действия (`detail`, `retry` если FAILED) |
| Пагинация | offset/limit, дефолт 50 на страницу |
| Counters | в шапке: «Сегодня: ✓ 1234 ⏳ 12 ✕ 3» (DELIVERED / QUEUED / FAILED) |

### Экран «Детали» (`/admin/notifications/{id}`)

- Полные поля `notifications` (включая `source_event_payload` в виде JSON-pretty).
- Список `delivery_attempts` хронологически.
- Кнопки `Retry` (если статус `FAILED`) и `Abandon` (если статус `FAILED`).

### Тексты ошибок UI

| Сценарий | Что показываем |
|---|---|
| Retry успешен | toast «Уведомление поставлено в очередь» |
| Retry на не-FAILED | toast-error «Можно ретраить только FAILED-уведомления» (404 от backend, маппим) |
| Abandon | confirm-диалог: «Это финальное действие, уведомление больше не будет отправлено. Уверены?» |
