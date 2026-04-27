---
type: service
context: notification-service
tier: A
ucp-level: 0
tags:
  - service
  - tech/java
  - tech/spring-boot
  - tech/jdbc-template
  - tech/postgres
  - tech/kafka
  - tier/A
  - bc/notification
  - case/marketplace
---

# Notification Service — Use Case спецификация (Tier A)

> Индекс. Спека разбита на 13 файлов — по одному на раздел шаблона. Tier A значит, что разделы 8 (Domain Events) и 12 (Saga) **пропущены**: мы не публикуем доменные события и не координируем процессы. Это контрпример к [Order Service (Tier C)](../../order-service/docs/spec/order-service.md), который показывает, что не каждому сервису нужны агрегаты и саги.

**Сервис:** транзакционные уведомления маркетплейса. Чистый event-consumer событий Order Service, отправляет email + push, журналирует доставку. Tier A / без UCP-уровня (UseCase Pattern и DDD overkill).

## Оглавление

| №   | Раздел                            | Файл                                      | Кто заполняет     |
| --- | --------------------------------- | ----------------------------------------- | ----------------- |
| 01  | Контекст / модуль                 | [01-notification-service-context](01-notification-service-context.md)         | БА + Архитектор   |
| 02  | Ubiquitous Language               | [02-notification-service-language](02-notification-service-language.md)       | БА                |
| 03  | Domain Model (ER)                 | [03-notification-service-model](03-notification-service-model.md)             | Архитектор + Dev  |
| 04  | Жизненный цикл уведомления        | [04-notification-service-lifecycle](04-notification-service-lifecycle.md)     | БА + Архитектор   |
| 05  | Роли и права                      | [05-notification-service-roles](05-notification-service-roles.md)             | БА                |
| 06  | Бизнес-правила (BR)               | [06-notification-service-rules](06-notification-service-rules.md)             | БА + Архитектор   |
| 07  | Операции (Commands)               | [07-notification-service-commands](07-notification-service-commands.md)       | Архитектор + Dev  |
| 08  | Domain Events                     | — (Tier A: не пишется)                                                        |                   |
| 09  | Queries                           | [09-notification-service-queries](09-notification-service-queries.md)         | Архитектор + Dev  |
| 10  | Use Cases                         | [10-notification-service-use-cases](10-notification-service-use-cases.md)     | БА                |
| 11  | UI-спецификация                   | [11-notification-service-ui](11-notification-service-ui.md)                   | БА                |
| 12  | Saga / Process Manager            | — (Tier A: не пишется)                                                        |                   |
| 13  | Каталог ошибок                    | [13-notification-service-errors](13-notification-service-errors.md)           | Dev               |
| 14  | Интеграции                        | [14-notification-service-integrations](14-notification-service-integrations.md)| Архитектор        |
| 15  | Критерии приёмки (AC)             | [15-notification-service-acceptance](15-notification-service-acceptance.md)   | БА + QA           |
| 16  | Нефункциональные требования       | [16-notification-service-nfr](16-notification-service-nfr.md)                 | Архитектор        |
| 17  | Стек технологий                   | [17-notification-service-stack](17-notification-service-stack.md)             | Архитектор + Dev  |

## Стек

Spring Boot · JdbcTemplate · PostgreSQL · Kafka consumer · Mailgun · FCM. Без агрегатов, без Outbox (мы не источник событий), без саг. Детали — [17-notification-service-stack](17-notification-service-stack.md).

## Связанные артефакты

- [Кейс: маркетплейс](https://vikulin-va.ru/case/) — бизнес-описание.
- [Notification Service — статья](https://vikulin-va.ru/case/notification-service/) — единый текст спеки на сайте.
- [Order Service](https://github.com/remodov/order-service) — источник событий, который потребляет Notification.
- [Use Case спецификация: универсальный шаблон](https://vikulin-va.ru/use-case-pattern/spec-template/) — что значит каждый Tier.
