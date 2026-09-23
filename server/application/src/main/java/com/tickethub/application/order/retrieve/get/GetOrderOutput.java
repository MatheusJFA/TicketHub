package com.tickethub.application.order.retrieve.get;

import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.payment.ChargeID;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record GetOrderOutput(
        String orderId,
        String customerId,
        String status,
        BigDecimal totalValue,
        String currency,
        Instant expiresAt,
        String chargeId,
        List<String> spotIds) {
    public static GetOrderOutput from(final Order order) {
        return new GetOrderOutput(
                order.getId().getValue(),
                order.getCustomerId().getValue(),
                order.getStatus().name(),
                order.getTotal().getValue(),
                order.getTotal().getCurrency().getCurrencyCode(),
                order.getExpiresAt(),
                Optional.ofNullable(order.getChargeId()).map(ChargeID::getValue).orElse(null),
                order.getItems().stream()
                        .map(item -> item.getSpotId().getValue())
                        .toList());
    }
}
