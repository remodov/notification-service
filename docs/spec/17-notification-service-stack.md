---
type: context-section
context: notification-service
parent: "[[notification-service]]"
section: stack
tier: A
tags:
  - stack
  - bc/notification
---

## 17. Стек технологий

```
Java 21
Spring Boot 3.4.x
Spring Kafka                  — консьюмер marketplace.orders.v1
Spring Web                    — REST для webhooks и admin API
Spring Security               — OAuth2 Resource Server + HMAC-фильтр
Spring JDBC + JdbcTemplate    — слой доступа к БД (без JPA, см. ниже)
PostgreSQL 16                 — notifications, templates, delivery_attempts, processed_events
Liquibase                     — схема и шаблоны как ChangeSet
Mailgun Java SDK              — outbound email
Firebase Admin SDK (Java)     — outbound push
Resilience4j                  — circuit breaker / retry для Customer BFF и провайдеров
Micrometer + Prometheus       — метрики
OpenTelemetry                 — distributed tracing
JUnit 5 + Testcontainers      — интеграционные тесты с реальным Postgres и Kafka в контейнере
```

### Почему `JdbcTemplate`, а не JPA

На Tier A нет агрегатов; работа с БД — это insert / update / select. `JdbcTemplate` (или `NamedParameterJdbcTemplate`) даёт прямой контроль над SQL без оверхеда `Entity`-mapping'а, что для CRUD-сервиса проще читать и оптимизировать. JPA на этом уровне — overengineering. На Tier B уже стоит подумать про jOOQ или Spring Data JPA, на Tier C — jOOQ обязателен.

### Расширения, которые **не делаем** в первой версии (Tier B/C)

- **Подписки и опт-ауты** — потребует таблицы `user_preferences` и UseCase'ы `UpdatePreferences` / `Unsubscribe`. Это уже честный Tier B.
- **A/B-тесты текстов** — требует разделения `experiment_variant` в `notifications`, отдельной аналитики. Tier B+.
- **Push на Web (WebPush)** — отдельный провайдер, не критично для запуска.
- **Кампании / массовые рассылки** — отдельный сервис, не Notification. Notification живёт **только** на транзакционных событиях.
- **Динамический выбор каналов через админку** — таблица `channel_rules` с приоритетами; Tier B.
