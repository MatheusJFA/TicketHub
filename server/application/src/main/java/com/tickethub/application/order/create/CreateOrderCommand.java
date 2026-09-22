package com.tickethub.application.order.create;

import java.util.List;

public record CreateOrderCommand(String customerId, List<String> spotIds, String idempotencyKey) {
    public static CreateOrderCommand with(final String customerId, final List<String> spotIds) {
        return new CreateOrderCommand(customerId, spotIds, null);
    }

    public static CreateOrderCommand with(final String customerId, final List<String> spotIds,
            final String idempotencyKey) {
        return new CreateOrderCommand(customerId, spotIds, idempotencyKey);
    }
}
