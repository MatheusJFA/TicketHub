package com.tickethub.infrastructure.order.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.tickethub.application.order.cancel.CancelOrderOutput;
import com.tickethub.application.order.create.CreateOrderOutput;
import com.tickethub.application.order.retrieve.get.GetOrderOutput;

public record OrderResponse(
        String orderId,
        String customerId,
        String status,
        BigDecimal totalValue,
        String currency,
        Instant expiresAt,
        String chargeId,
        List<String> spotIds) {
    public static OrderResponse from(final CreateOrderOutput output) {
        return new OrderResponse(
                output.orderId(),
                output.customerId(),
                output.status(),
                output.totalValue(),
                output.currency(),
                output.expiresAt(),
                null,
                output.spotIds());
    }

    public static OrderResponse from(final GetOrderOutput output) {
        return new OrderResponse(
                output.orderId(),
                output.customerId(),
                output.status(),
                output.totalValue(),
                output.currency(),
                output.expiresAt(),
                output.chargeId(),
                output.spotIds());
    }

    public static OrderResponse from(final CancelOrderOutput output) {
        return new OrderResponse(
                output.orderId(),
                output.customerId(),
                output.status(),
                output.totalValue(),
                output.currency(),
                output.expiresAt(),
                output.chargeId(),
                output.spotIds());
    }
}
