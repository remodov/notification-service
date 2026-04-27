package ru.vikulinva.notificationservice.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.vikulinva.notificationservice.domain.Channel;
import ru.vikulinva.notificationservice.domain.Notification;
import ru.vikulinva.notificationservice.domain.NotificationStatus;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * JdbcTemplate-репозиторий для {@code notifications}. На Tier A — голый
 * SQL без JPA: понятно, что именно крутится в БД, легко оптимизировать.
 */
@Repository
public class NotificationRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public NotificationRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    public void insert(Notification n) {
        String sql = """
            INSERT INTO notifications (
                id, event_id, event_type, user_id, channel, contact, template_key, locale,
                status, source_event_payload, template_variables, external_id,
                created_at, sent_at, delivered_at, last_error
            ) VALUES (
                :id, :eventId, :eventType, :userId, :channel, :contact, :templateKey, :locale,
                :status, :sourcePayload::jsonb, :templateVars::jsonb, :externalId,
                :createdAt, :sentAt, :deliveredAt, :lastError
            )
            """;
        jdbc.update(sql, params(n));
    }

    public Optional<Notification> findById(UUID id) {
        var rows = jdbc.query(
            "SELECT * FROM notifications WHERE id = :id",
            new MapSqlParameterSource("id", id),
            rowMapper());
        return rows.stream().findFirst();
    }

    public List<Notification> findPending(int limit) {
        return jdbc.query(
            "SELECT * FROM notifications WHERE status = 'QUEUED' ORDER BY created_at LIMIT :limit",
            new MapSqlParameterSource("limit", limit),
            rowMapper());
    }

    public Optional<Notification> findByExternalId(String externalId) {
        var rows = jdbc.query(
            "SELECT * FROM notifications WHERE external_id = :externalId",
            new MapSqlParameterSource("externalId", externalId),
            rowMapper());
        return rows.stream().findFirst();
    }

    /**
     * Поиск с фильтрами для оператора. Все параметры опциональны.
     */
    public List<Notification> search(Filter filter, int page, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM notifications WHERE 1=1");
        var params = new MapSqlParameterSource();
        if (filter.userId() != null) {
            sql.append(" AND user_id = :userId");
            params.addValue("userId", filter.userId());
        }
        if (filter.status() != null) {
            sql.append(" AND status = :status");
            params.addValue("status", filter.status().name());
        }
        if (filter.channel() != null) {
            sql.append(" AND channel = :channel");
            params.addValue("channel", filter.channel().name());
        }
        if (filter.eventType() != null) {
            sql.append(" AND event_type = :eventType");
            params.addValue("eventType", filter.eventType());
        }
        if (filter.from() != null) {
            sql.append(" AND created_at >= :from");
            params.addValue("from", Timestamp.from(filter.from()));
        }
        if (filter.to() != null) {
            sql.append(" AND created_at < :to");
            params.addValue("to", Timestamp.from(filter.to()));
        }
        sql.append(" ORDER BY created_at DESC LIMIT :size OFFSET :offset");
        params.addValue("size", size);
        params.addValue("offset", (long) page * size);
        return jdbc.query(sql.toString(), params, rowMapper());
    }

    public long count(Filter filter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM notifications WHERE 1=1");
        var params = new MapSqlParameterSource();
        if (filter.userId() != null) {
            sql.append(" AND user_id = :userId");
            params.addValue("userId", filter.userId());
        }
        if (filter.status() != null) {
            sql.append(" AND status = :status");
            params.addValue("status", filter.status().name());
        }
        if (filter.channel() != null) {
            sql.append(" AND channel = :channel");
            params.addValue("channel", filter.channel().name());
        }
        if (filter.eventType() != null) {
            sql.append(" AND event_type = :eventType");
            params.addValue("eventType", filter.eventType());
        }
        if (filter.from() != null) {
            sql.append(" AND created_at >= :from");
            params.addValue("from", Timestamp.from(filter.from()));
        }
        if (filter.to() != null) {
            sql.append(" AND created_at < :to");
            params.addValue("to", Timestamp.from(filter.to()));
        }
        Long c = jdbc.queryForObject(sql.toString(), params, Long.class);
        return c == null ? 0 : c;
    }

    public void updateStatus(UUID id, NotificationStatus status, Instant sentAt, String externalId, String lastError) {
        jdbc.update("""
            UPDATE notifications
            SET status = :status,
                sent_at = COALESCE(:sentAt, sent_at),
                external_id = COALESCE(:externalId, external_id),
                last_error = :lastError
            WHERE id = :id
            """, new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("status", status.name())
            .addValue("sentAt", sentAt == null ? null : Timestamp.from(sentAt))
            .addValue("externalId", externalId)
            .addValue("lastError", lastError));
    }

    public void markDelivered(UUID id, Instant deliveredAt) {
        jdbc.update("""
            UPDATE notifications
            SET status = 'DELIVERED', delivered_at = :deliveredAt
            WHERE id = :id AND status = 'SENT'
            """, new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("deliveredAt", Timestamp.from(deliveredAt)));
    }

    public void markBounced(UUID id, String reason) {
        jdbc.update("""
            UPDATE notifications
            SET status = 'BOUNCED', last_error = :reason
            WHERE id = :id AND status = 'SENT'
            """, new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("reason", reason));
    }

    public int deleteOlderThan(Instant threshold) {
        return jdbc.update(
            "DELETE FROM notifications WHERE created_at < :threshold",
            new MapSqlParameterSource("threshold", Timestamp.from(threshold)));
    }

    private MapSqlParameterSource params(Notification n) {
        var p = new MapSqlParameterSource();
        p.addValue("id", n.getId());
        p.addValue("eventId", n.getEventId());
        p.addValue("eventType", n.getEventType());
        p.addValue("userId", n.getUserId());
        p.addValue("channel", n.getChannel().name());
        p.addValue("contact", n.getContact());
        p.addValue("templateKey", n.getTemplateKey());
        p.addValue("locale", n.getLocale());
        p.addValue("status", n.getStatus().name());
        p.addValue("sourcePayload", n.getSourceEventPayload());
        p.addValue("templateVars", n.getTemplateVariables());
        p.addValue("externalId", n.getExternalId());
        p.addValue("createdAt", Timestamp.from(n.getCreatedAt()));
        p.addValue("sentAt", n.getSentAt() == null ? null : Timestamp.from(n.getSentAt()));
        p.addValue("deliveredAt", n.getDeliveredAt() == null ? null : Timestamp.from(n.getDeliveredAt()));
        p.addValue("lastError", n.getLastError());
        return p;
    }

    private RowMapper<Notification> rowMapper() {
        return (rs, rn) -> {
            Notification n = new Notification();
            n.setId(rs.getObject("id", UUID.class));
            n.setEventId(rs.getObject("event_id", UUID.class));
            n.setEventType(rs.getString("event_type"));
            n.setUserId(rs.getObject("user_id", UUID.class));
            n.setChannel(Channel.valueOf(rs.getString("channel")));
            n.setContact(rs.getString("contact"));
            n.setTemplateKey(rs.getString("template_key"));
            n.setLocale(rs.getString("locale"));
            n.setStatus(NotificationStatus.valueOf(rs.getString("status")));
            n.setSourceEventPayload(rs.getString("source_event_payload"));
            n.setTemplateVariables(rs.getString("template_variables"));
            n.setExternalId(rs.getString("external_id"));
            n.setCreatedAt(rs.getTimestamp("created_at").toInstant());
            var sentAt = rs.getTimestamp("sent_at");
            n.setSentAt(sentAt == null ? null : sentAt.toInstant());
            var deliveredAt = rs.getTimestamp("delivered_at");
            n.setDeliveredAt(deliveredAt == null ? null : deliveredAt.toInstant());
            n.setLastError(rs.getString("last_error"));
            return n;
        };
    }

    public record Filter(
        UUID userId,
        NotificationStatus status,
        Channel channel,
        String eventType,
        Instant from,
        Instant to
    ) {
        public static Filter empty() { return new Filter(null, null, null, null, null, null); }
    }
}
