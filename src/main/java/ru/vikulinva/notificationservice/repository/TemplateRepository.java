package ru.vikulinva.notificationservice.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.vikulinva.notificationservice.domain.Template;

import java.util.Objects;
import java.util.Optional;

@Repository
public class TemplateRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public TemplateRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    /**
     * Поиск с fallback на дефолтную локаль ({@code ru}). Если шаблона
     * под запрошенный {@code locale} нет — отдаём русский.
     */
    public Optional<Template> find(String key, String locale) {
        var rows = jdbc.query("""
            SELECT key, locale, subject, body
            FROM templates
            WHERE key = :key AND locale IN (:locale, 'ru')
            ORDER BY CASE WHEN locale = :locale THEN 0 ELSE 1 END
            LIMIT 1
            """, new MapSqlParameterSource()
            .addValue("key", key)
            .addValue("locale", locale),
            (rs, rn) -> new Template(
                rs.getString("key"),
                rs.getString("locale"),
                rs.getString("subject"),
                rs.getString("body")));
        return rows.stream().findFirst();
    }
}
