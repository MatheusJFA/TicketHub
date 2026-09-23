package com.tickethub.infrastructure.payment.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;

/**
 * Mercado Pago notification envelope. The payment id preferably arrives as
 * the {@code data.id} query param; the body is a fallback. The status is
 * never read from here — it is always fetched from the MP API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoPagoNotification(String type, NotificationData data) {

    public String dataId() {
        return Optional.ofNullable(data).map(NotificationData::id).orElse(null);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NotificationData(String id) {}
}
