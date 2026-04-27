package ru.vikulinva.notificationservice.controller.dto;

import org.springframework.stereotype.Component;
import ru.vikulinva.notificationservice.domain.DeliveryAttempt;
import ru.vikulinva.notificationservice.domain.Notification;

import java.util.List;

/**
 * Маппинг доменных POJO → REST-DTO. На Tier A — простые методы вместо
 * MapStruct: меньше зависимостей, проще читать.
 */
@Component
public class RestMapper {

    public NotificationSummaryDto toSummary(Notification n) {
        return new NotificationSummaryDto(
            n.getId(),
            n.getUserId(),
            n.getEventType(),
            n.getChannel(),
            n.getContact(),
            n.getStatus(),
            n.getCreatedAt(),
            n.getSentAt(),
            n.getDeliveredAt());
    }

    public NotificationDetailDto toDetail(Notification n, List<DeliveryAttempt> attempts) {
        var attemptDtos = attempts.stream()
            .map(a -> new NotificationDetailDto.DeliveryAttemptDto(
                a.id(),
                a.attemptNumber(),
                a.result().name(),
                a.responseSnippet(),
                a.attemptedAt()))
            .toList();
        return new NotificationDetailDto(
            toSummary(n),
            n.getTemplateKey(),
            n.getLocale(),
            n.getExternalId(),
            n.getLastError(),
            n.getSourceEventPayload(),
            n.getTemplateVariables(),
            attemptDtos);
    }
}
