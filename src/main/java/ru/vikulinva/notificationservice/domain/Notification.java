package ru.vikulinva.notificationservice.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Запись журнала уведомлений. На Tier A это POJO, не агрегат.
 * Все поля nullable там, где это разрешено схемой; mutable где нужны
 * status-переходы. Никаких инвариантов внутри — все правила в сервисе.
 */
public class Notification {

    private UUID id;
    private UUID eventId;
    private String eventType;
    private UUID userId;
    private Channel channel;
    private String contact;
    private String templateKey;
    private String locale;
    private NotificationStatus status;
    private String sourceEventPayload;
    private String templateVariables;
    private String externalId;
    private Instant createdAt;
    private Instant sentAt;
    private Instant deliveredAt;
    private String lastError;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Channel getChannel() { return channel; }
    public void setChannel(Channel channel) { this.channel = channel; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }

    public String getSourceEventPayload() { return sourceEventPayload; }
    public void setSourceEventPayload(String sourceEventPayload) { this.sourceEventPayload = sourceEventPayload; }

    public String getTemplateVariables() { return templateVariables; }
    public void setTemplateVariables(String templateVariables) { this.templateVariables = templateVariables; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}
