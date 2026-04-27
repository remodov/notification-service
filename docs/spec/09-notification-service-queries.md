---
type: context-section
context: notification-service
parent: "[[notification-service]]"
section: queries
tier: A
tags:
  - queries
  - bc/notification
---

## 9. Queries / Read Model

Чтения, нужные оператору и Customer BFF.

| Запрос | Кому | Параметры | Результат |
|---|---|---|---|
| Список уведомлений с фильтром | оператор | `userId?`, `status?`, `eventType?`, `channel?`, `from`, `to`, пагинация | страница `Notification` с базовыми полями |
| Детали уведомления | оператор | `notificationId` | полная запись + список `delivery_attempts` |
| Inbox пользователя | Customer BFF (service-to-service) | `userId`, пагинация | страница уведомлений канала `EMAIL`+`PUSH`, отсортированных по дате |
| Метрики дашборда | оператор / SRE | `from`, `to` | агрегаты по статусам, по каналам, по типам событий |

Read Model на Tier A — это просто SELECT-запросы поверх той же таблицы `notifications`. Отдельная Read Model появляется на Tier B+ (например, материализованная view для метрик).
