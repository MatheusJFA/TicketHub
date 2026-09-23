package com.tickethub.application.order.create;

import com.tickethub.domain.core.order.Order;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CreateOrderOutput(
        String orderId,
        String customerId,
        String status,
        BigDecimal totalValue,
        String currency,
        Instant expiresAt,
        List<String> spotIds) {
    public static CreateOrderOutput from(final Order order) {
        return new CreateOrderOutput(
                order.getId().getValue(),
                order.getCustomerId().getValue(),
                order.getStatus().name(),
                order.getTotal().getValue(),
                order.getTotal().getCurrency().getCurrencyCode(),
                order.getExpiresAt(),
                order.getItems().stream()
                        .map(item -> item.getSpotId().getValue())
                        .toList());
    }
}
