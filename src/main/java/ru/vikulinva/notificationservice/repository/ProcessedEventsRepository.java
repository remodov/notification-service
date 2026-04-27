package ru.vikulinva.notificationservice.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Журнал уже обработанных Kafka-событий для идемпотентного консьюмера.
 * См. spec BR-N1.
 */
@Repository
public class ProcessedEventsRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ProcessedEventsRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    /**
     * @return {@code true} если запись только что вставлена; {@code false} если
     *         событие уже было обработано (повторная доставка).
     */
    public boolean markProcessed(UUID eventId, String eventType, Instant processedAt) {
        int inserted = jdbc.update("""
            INSERT INTO processed_events (event_id, event_type, processed_at)
            VALUES (:eventId, :eventType, :processedAt)
            ON CONFLICT (event_id) DO NOTHING
            """, new MapSqlParameterSource()
            .addValue("eventId", eventId)
            .addValue("eventType", eventType)
            .addValue("processedAt", Timestamp.from(processedAt)));
        return inserted > 0;
    }
}
