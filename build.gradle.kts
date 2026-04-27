plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "ru.vikulinva"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }
}

dependencies {
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.jdbc)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.kafka)

    runtimeOnly(libs.postgresql)
    implementation(libs.liquibase.core)
    implementation(libs.hikaricp)

    implementation(libs.resilience4j.spring.boot3)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.jsr310)
    implementation(libs.micrometer.prometheus)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.postgres)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.spring.kafka.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.wiremock)
    testImplementation(libs.wiremock.jetty12)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito.junit.jupiter)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all", "-Xlint:-processing"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // Каждый тестовый класс запускается в свежем JVM-процессе. Это
    // развязывает классы по WireMock-портам и состоянию singleton-бинов
    // (Resilience4j circuit breaker, Mockito-моки), которые нельзя надёжно
    // изолировать через @MockitoBean при кешировании ApplicationContext.
    forkEvery = 1
    maxParallelForks = 1
}

springBoot {
    mainClass.set("ru.vikulinva.notificationservice.NotificationServiceApplication")
}

sourceSets {
    main {
        resources {
            // Liquibase changelog лежит в migrations/ на уровне репо.
            srcDir(rootProject.file("migrations"))
        }
    }
}
