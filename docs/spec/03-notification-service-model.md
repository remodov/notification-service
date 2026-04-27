---
type: context-section
context: notification-service
parent: "[[notification-service]]"
section: model
tier: A
tags:
  - model
  - bc/notification
---

## 3. Domain Model

На Tier A раздел сводится к ER-схеме и списку таблиц. Никаких агрегатов, value objects, доменных событий — это **проектное** решение, не упущение.

### ER-схема

```mermaid
erDiagram
    NOTIFICATIONS ||--|| TEMPLATES : "uses"
    NOTIFICATIONS ||--o{ DELIVERY_ATTEMPTS : "has"
    NOTIFICATIONS }o--|| PROCESSED_EVENTS : "dedup by"

    NOTIFICATIONS {
        uuid id PK
        uuid event_id "из заголовка Kafka, для идемпотентности"
        varchar event_type "OrderConfirmed, OrderPaid, ..."
        uuid user_id "получатель"
        varchar channel "EMAIL | PUSH"
        varchar contact "email или push-token, материализован"
        varchar template_key "order.confirmed.email"
        varchar locale "ru, en"
        varchar status "QUEUED → SENT → DELIVERED|BOUNCED|FAILED"
        jsonb source_event_payload "для retry и дебага"
        jsonb template_variables "подставленные значения"
        timestamptz created_at
        timestamptz sent_at "когда ушло в провайдер"
        timestamptz delivered_at "из webhook-а, nullable"
        text last_error "при FAILED — текст ошибки провайдера"
    }

    TEMPLATES {
        varchar key PK "order.confirmed.email"
        varchar locale PK
        varchar subject "Заказ var-order_id подтверждён"
        text body "Тело с подстановкой var-customer_name и var-order_id"
        timestamptz updated_at
    }

    DELIVERY_ATTEMPTS {
        uuid id PK
        uuid notification_id FK
        int attempt_number
        varchar result "OK | TRANSIENT_ERROR | PERMANENT_ERROR"
        text response_snippet "первые 500 символов ответа провайдера"
        timestamptz attempted_at
    }

    PROCESSED_EVENTS {
        uuid event_id PK
        timestamptz processed_at
    }
```

### Таблицы — словами

- **`notifications`** — главная таблица. Одна строка = одна попытка доставки на один канал. Хранит копию контакта на момент отправки (email мог поменяться) и копию шаблонных переменных (для retry без обращения к Customer BFF).
- **`templates`** — шаблоны письма. Ключ = `<event_type>.<channel>` (например `order.confirmed.email`). Язык — `locale`. Тело может содержать `${var}` — подстановка простой `String.replace`.
- **`delivery_attempts`** — лог каждой попытки отправки. Изолирован от `notifications` чтобы запросы по статусу шли по индексу без сканирования больших `jsonb`-полей.
- **`processed_events`** — журнал обработанных Kafka-событий для идемпотентности консьюмера. PK по `event_id` — повторная доставка от Kafka не создаст дубль уведомления.

### Что **НЕ делается**

- Tier A не вводит модели UseCase-входов (`SendNotificationRequest` и т.п.). Контроллер / сервис принимают сразу JSON-DTO. Это ОК для CRUD-сервиса.
- Нет агрегатов, value objects, доменных событий.
