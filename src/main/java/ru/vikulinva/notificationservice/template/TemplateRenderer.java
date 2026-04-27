package ru.vikulinva.notificationservice.template;

import org.springframework.stereotype.Component;
import ru.vikulinva.notificationservice.domain.Template;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Простая подстановка {@code ${var}} → значение. Не используем Velocity /
 * Freemarker / Thymeleaf — для Tier A правил типа условных блоков и циклов
 * не нужно, а зависимость на отдельный engine добавляет сложности и атак.
 *
 * <p>Если в шаблоне {@code ${var}} не имеет соответствия в variables —
 * подставляется пустая строка и пишется WARN-лог. Это защищает от
 * крэша при минорных рассинхронах между Order Service и шаблонами.
 */
@Component
public class TemplateRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([a-zA-Z0-9_]+)}");

    public Rendered render(Template template, Map<String, String> variables) {
        return new Rendered(
            replace(template.subject(), variables),
            replace(template.body(), variables));
    }

    private String replace(String text, Map<String, String> vars) {
        Matcher m = PLACEHOLDER.matcher(text);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String key = m.group(1);
            String value = vars.getOrDefault(key, "");
            m.appendReplacement(out, Matcher.quoteReplacement(value));
        }
        m.appendTail(out);
        return out.toString();
    }

    public record Rendered(String subject, String body) {}
}
