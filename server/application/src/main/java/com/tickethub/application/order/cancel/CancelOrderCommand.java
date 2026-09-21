package com.tickethub.application.order.cancel;

public record CancelOrderCommand(String orderId) {
    public static CancelOrderCommand with(final String orderId) {
        return new CancelOrderCommand(orderId);
    }
}
