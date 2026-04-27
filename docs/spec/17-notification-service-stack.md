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
spring-boot-starter-jooq      — слой доступа к БД (см. ниже)
PostgreSQL 16                 — notifications, templates, delivery_attempts, processed_events
                                + ENUM-типы notification_channel/notification_status/delivery_attempt_result
Liquibase                     — схема и шаблоны как ChangeSet
nu.studer.jooq 10.x           — кодогенерация POJO/Records/Tables/Enums из applied-схемы
Mailgun (REST)                — outbound email
Firebase Cloud Messaging (REST) — outbound push
Resilience4j                  — circuit breaker / retry для Customer BFF
Micrometer + Prometheus       — метрики
OpenTelemetry                 — distributed tracing
JUnit 5 + WireMock            — интеграционные тесты с реальным Postgres и WireMock-стабами
```

### Почему `jOOQ`, и почему **только** generated классы

**jOOQ** — единый persistence-стандарт во всех сервисах команды независимо
от Tier'а. Других вариантов (JdbcTemplate, JPA, MyBatis) не используем —
это решение принято на уровне команды и фиксирует его одно правило
`BS-17` из [spring-bootstrap-style-guide](https://vikulin-va.ru/use-case-pattern/library/).

**Только сгенерированные классы.** Цель — меньше кода и один источник
правды (Liquibase-схема → jOOQ codegen → Java). Вместо handcrafted
`Notification.java` / `Channel.java` / `NotificationStatus.java`,
дублирующих строку БД и enum-значения, мы используем сгенеренные
`NotificationsPojo`, `NotificationChannel`, `NotificationStatus` из
пакета `generated`. Если на enum нужны методы (например, `isTerminal()`)
— inline'им проверку на use-sites или кладём в utility-класс,
**generated-классы не модифицируем**.

Tier A не означает «упростить persistence-слой». Tier A — про отсутствие
DDD-агрегатов и UseCase Pattern. Persistence остаётся на jOOQ.

### Расширения, которые **не делаем** в первой версии (Tier B/C)

- **Подписки и опт-ауты** — потребует таблицы `user_preferences` и UseCase'ы `UpdatePreferences` / `Unsubscribe`. Это уже честный Tier B.
- **A/B-тесты текстов** — требует разделения `experiment_variant` в `notifications`, отдельной аналитики. Tier B+.
- **Push на Web (WebPush)** — отдельный провайдер, не критично для запуска.
- **Кампании / массовые рассылки** — отдельный сервис, не Notification. Notification живёт **только** на транзакционных событиях.
- **Динамический выбор каналов через админку** — таблица `channel_rules` с приоритетами; Tier B.
