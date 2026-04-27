package ru.vikulinva.notificationservice.channel;

import org.springframework.stereotype.Component;
import ru.vikulinva.notificationservice.domain.Channel;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BR-N2: фиксированная таблица «event-type → набор каналов». На Tier A
 * это hard-coded mapping в коде; Tier B вынесет в БД с правилами через
 * админку.
 */
@Component
public class ChannelRules {

    private static final Map<String, Set<Channel>> CHANNELS = Map.ofEntries(
        Map.entry("OrderConfirmed",  Set.of(Channel.EMAIL, Channel.PUSH)),
        Map.entry("OrderPaid",       Set.of(Channel.EMAIL, Channel.PUSH)),
        Map.entry("OrderShipped",    Set.of(Channel.EMAIL, Channel.PUSH)),
        Map.entry("OrderDelivered",  Set.of(Channel.EMAIL)),
        Map.entry("OrderCancelled",  Set.of(Channel.EMAIL, Channel.PUSH)),
        Map.entry("OrderRefunded",   Set.of(Channel.EMAIL, Channel.PUSH)),
        Map.entry("DisputeOpened",   Set.of(Channel.PUSH)),
        Map.entry("DisputeResolved", Set.of(Channel.EMAIL, Channel.PUSH))
    );

    public List<Channel> channelsFor(String eventType) {
        Set<Channel> set = CHANNELS.get(eventType);
        return set == null ? List.of() : set.stream().sorted().toList();
    }

    /**
     * Ключ шаблона для пары event-type + channel. Локаль выбирается отдельно.
     * Пример: {@code OrderConfirmed} + {@code EMAIL} → {@code "order.confirmed.email"}.
     */
    public String templateKey(String eventType, Channel channel) {
        String base = switch (eventType) {
            case "OrderConfirmed"  -> "order.confirmed";
            case "OrderPaid"       -> "order.paid";
            case "OrderShipped"    -> "order.shipped";
            case "OrderDelivered"  -> "order.delivered";
            case "OrderCancelled"  -> "order.cancelled";
            case "OrderRefunded"   -> "order.refunded";
            case "DisputeOpened"   -> "dispute.opened";
            case "DisputeResolved" -> "dispute.resolved";
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
        return base + "." + channel.name().toLowerCase();
    }
}
