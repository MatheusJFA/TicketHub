package com.tickethub.infrastructure.webhooks;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.core.spot.SpotGateway;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.ObjectMapper;

@Component
public class PartnerWebhookDispatcher {

    private static final Logger log = LoggerFactory.getLogger(PartnerWebhookDispatcher.class);

    private final OrderGateway orders;
    private final SpotGateway spots;
    private final ShowGateway shows;
    private final PartnerGateway partners;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Retry retry;

    public PartnerWebhookDispatcher(
            final OrderGateway orders,
            final SpotGateway spots,
            final ShowGateway shows,
            final PartnerGateway partners,
            final RestClient restClient,
            final ObjectMapper objectMapper,
            final PartnerWebhookProperties properties) {
        this.orders = requireNonNull(orders, "'orders' should not be null");
        this.spots = requireNonNull(spots, "'spots' should not be null");
        this.shows = requireNonNull(shows, "'shows' should not be null");
        this.partners = requireNonNull(partners, "'partners' should not be null");
        this.restClient = requireNonNull(restClient, "'restClient' should not be null");
        this.objectMapper = requireNonNull(objectMapper, "'objectMapper' should not be null");
        requireNonNull(properties, "'properties' should not be null");
        this.retry = Retry.of(
                "partner-webhook",
                RetryConfig.custom()
                        .maxAttempts(Math.max(1, properties.getMaxAttempts()))
                        .waitDuration(properties.getRetryWait())
                        .retryExceptions(RestClientException.class)
                        .build());
    }

    public void dispatchPaid(final String orderId, final String occurredOn) {
        dispatch("order.paid", orderId, occurredOn);
    }

    public void dispatchRefunded(final String orderId, final String occurredOn) {
        dispatch("order.refunded", orderId, occurredOn);
    }

    private void dispatch(final String type, final String orderId, final String occurredOn) {
        final var eventId = orderId + ":" + type;
        final Optional<Delivery> delivery = resolve(orderId, type, occurredOn, eventId);
        if (delivery.isEmpty()) {
            return;
        }
        final var target = delivery.get();
        try {
            Retry.decorateCheckedSupplier(retry, () -> {
                        post(target);
                        return null;
                    })
                    .get();
            log.info("Partner webhook delivered eventId={} url={}", eventId, target.url());
        } catch (final Throwable e) {
            throw new PartnerWebhookException("Partner webhook delivery failed eventId=" + eventId, e);
        }
    }

    private Optional<Delivery> resolve(
            final String orderId, final String type, final String occurredOn, final String eventId) {
        final Optional<Order> order = orders.findById(OrderID.from(orderId));
        if (order.isEmpty() || order.get().getItems().isEmpty()) {
            log.info("Partner webhook skipped eventId={}: order missing", eventId);
            return Optional.empty();
        }
        final var current = order.get();
        final var placement = spots.findPlacement(current.getItems().get(0).getSpotId());
        if (placement.isEmpty()) {
            log.info("Partner webhook skipped eventId={}: spot placement missing", eventId);
            return Optional.empty();
        }
        final var show = shows.findById(ShowID.from(placement.get().showId()));
        if (show.isEmpty()) {
            log.info("Partner webhook skipped eventId={}: show missing", eventId);
            return Optional.empty();
        }
        final Optional<Partner> partner = partners.findById(show.get().getPartnerId());
        final var webhookUrl = partner.map(Partner::getWebhookUrl).orElse(null);
        if (partner.isEmpty() || webhookUrl == null || webhookUrl.isBlank()) {
            log.info("Partner webhook skipped eventId={}: partner has no webhook", eventId);
            return Optional.empty();
        }
        final var body = new WebhookPayload(
                eventId,
                type,
                orderId,
                show.get().getId().getValue(),
                current.getTotal().getValue().toPlainString(),
                current.getTotal().getCurrency().getCurrencyCode(),
                current.getStatus().name(),
                occurredOn);
        final String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (final RuntimeException e) {
            log.warn("Partner webhook skipped eventId={}: cannot serialize payload", eventId);
            return Optional.empty();
        }
        return Optional.of(new Delivery(webhookUrl, partner.get().getWebhookSecret(), json, eventId, type));
    }

    private void post(final Delivery delivery) {
        final var request = restClient
                .post()
                .uri(delivery.url())
                .header("X-Tickethub-Event", delivery.type())
                .header("X-Tickethub-Delivery", delivery.eventId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(delivery.json());
        if (delivery.secret() != null && !delivery.secret().isBlank()) {
            request.header("X-Tickethub-Signature", hmacHex(delivery.secret(), delivery.json()));
        }
        request.retrieve().toBodilessEntity();
    }

    static String hmacHex(final String secret, final String message) {
        try {
            final var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (final NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Cannot sign partner webhook payload", e);
        }
    }

    record Delivery(String url, String secret, String json, String eventId, String type) {}

    record WebhookPayload(
            String eventId,
            String type,
            String orderId,
            String showId,
            String total,
            String currency,
            String orderStatus,
            String occurredOn) {}
}
