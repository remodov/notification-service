package ru.vikulinva.notificationservice.controller.dto;

import ru.vikulinva.notificationservice.domain.Channel;
import ru.vikulinva.notificationservice.domain.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

public record NotificationSummaryDto(
    UUID id,
    UUID userId,
    String eventType,
    Channel channel,
    String contact,
    NotificationStatus status,
    Instant createdAt,
    Instant sentAt,
    Instant deliveredAt
) {
}
