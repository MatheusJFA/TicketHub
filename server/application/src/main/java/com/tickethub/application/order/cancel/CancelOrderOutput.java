package com.tickethub.application.order.cancel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.payment.ChargeID;

public record CancelOrderOutput(
        String orderId,
        String customerId,
        String status,
        BigDecimal totalValue,
        String currency,
        Instant expiresAt,
        String chargeId,
        List<String> spotIds) {
    public static CancelOrderOutput from(final Order order) {
        return new CancelOrderOutput(
                order.getId().getValue(),
                order.getCustomerId().getValue(),
                order.getStatus().name(),
                order.getTotal().getValue(),
                order.getTotal().getCurrency().getCurrencyCode(),
                order.getExpiresAt(),
                Optional.ofNullable(order.getChargeId()).map(ChargeID::getValue).orElse(null),
                order.getItems().stream().map(item -> item.getSpotId().getValue()).toList());
    }
}
