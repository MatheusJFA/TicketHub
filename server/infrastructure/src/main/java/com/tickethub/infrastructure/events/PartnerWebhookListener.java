package com.tickethub.infrastructure.events;

import static java.util.Objects.requireNonNull;

import com.tickethub.infrastructure.webhooks.PartnerWebhookDispatcher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class PartnerWebhookListener {

    private final PartnerWebhookDispatcher dispatcher;
    private final ObjectMapper objectMapper;

    public PartnerWebhookListener(final PartnerWebhookDispatcher dispatcher, final ObjectMapper objectMapper) {
        this.dispatcher = requireNonNull(dispatcher, "'dispatcher' should not be null");
        this.objectMapper = requireNonNull(objectMapper, "'objectMapper' should not be null");
    }

    // Dedicated group: listeners in the same group split partitions, and spot
    // messages landing here would be consumed and lost to the spot listener.
    @KafkaListener(topics = "${tickethub.kafka.topic.name}", groupId = "tickethub-webhooks")
    public void onMessage(final String payload) {
        final OrderEventMessage message;
        try {
            message = objectMapper.readValue(payload, OrderEventMessage.class);
        } catch (final RuntimeException e) {
            return;
        }
        if (message.type() == null) {
            return;
        }
        switch (message.type()) {
            case OrderEventMessage.PAID_TYPE -> dispatcher.dispatchPaid(message.orderId(), message.occurredOn());
            case OrderEventMessage.REFUNDED_TYPE ->
                dispatcher.dispatchRefunded(message.orderId(), message.occurredOn());
            default -> {
                // Other event types belong to other listeners.
            }
        }
    }
}
