package ru.vikulinva.notificationservice.domain;

import java.time.Instant;
import java.util.UUID;

public record DeliveryAttempt(
    UUID id,
    UUID notificationId,
    int attemptNumber,
    AttemptResult result,
    String responseSnippet,
    Instant attemptedAt
) {
    public enum AttemptResult {
        OK,
        TRANSIENT_ERROR,
        PERMANENT_ERROR
    }
}
