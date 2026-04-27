package ru.vikulinva.notificationservice.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.vikulinva.notificationservice.domain.DeliveryAttempt;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
public class DeliveryAttemptRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public DeliveryAttemptRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    public void insert(DeliveryAttempt a) {
        jdbc.update("""
            INSERT INTO delivery_attempts (id, notification_id, attempt_number, result, response_snippet, attempted_at)
            VALUES (:id, :notificationId, :attemptNumber, :result, :responseSnippet, :attemptedAt)
            """, new MapSqlParameterSource()
            .addValue("id", a.id())
            .addValue("notificationId", a.notificationId())
            .addValue("attemptNumber", a.attemptNumber())
            .addValue("result", a.result().name())
            .addValue("responseSnippet", a.responseSnippet())
            .addValue("attemptedAt", Timestamp.from(a.attemptedAt())));
    }

    public int countAttempts(UUID notificationId) {
        Integer c = jdbc.queryForObject(
            "SELECT COUNT(*) FROM delivery_attempts WHERE notification_id = :id",
            new MapSqlParameterSource("id", notificationId),
            Integer.class);
        return c == null ? 0 : c;
    }

    public List<DeliveryAttempt> findByNotificationId(UUID notificationId) {
        return jdbc.query("""
            SELECT id, notification_id, attempt_number, result, response_snippet, attempted_at
            FROM delivery_attempts
            WHERE notification_id = :id
            ORDER BY attempt_number
            """, new MapSqlParameterSource("id", notificationId),
            (rs, rn) -> new DeliveryAttempt(
                rs.getObject("id", UUID.class),
                rs.getObject("notification_id", UUID.class),
                rs.getInt("attempt_number"),
                DeliveryAttempt.AttemptResult.valueOf(rs.getString("result")),
                rs.getString("response_snippet"),
                rs.getTimestamp("attempted_at").toInstant()));
    }
}
