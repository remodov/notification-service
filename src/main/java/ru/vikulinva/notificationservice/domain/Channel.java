package ru.vikulinva.notificationservice.domain;

/**
 * Канал доставки уведомления. На запуске два — EMAIL и PUSH.
 * SMS добавится отдельным провайдером без переписывания.
 */
public enum Channel {
    EMAIL,
    PUSH
}
