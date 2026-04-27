package ru.vikulinva.notificationservice.domain;

/**
 * Жизненный цикл уведомления. См. spec §4.
 *
 * <p>Терминальные состояния: {@link #DELIVERED}, {@link #BOUNCED} и
 * {@link #FAILED} (после ручной отметки оператора «безнадёжно» оно
 * остаётся {@code FAILED} и больше не ретраится).
 */
public enum NotificationStatus {
    QUEUED,
    SENT,
    DELIVERED,
    BOUNCED,
    FAILED;

    public boolean isTerminal() {
        return this == DELIVERED || this == BOUNCED;
    }

    public boolean canRetry() {
        return this == FAILED;
    }
}
