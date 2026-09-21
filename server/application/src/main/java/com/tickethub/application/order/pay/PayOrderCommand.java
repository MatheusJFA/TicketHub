package com.tickethub.application.order.pay;

public record PayOrderCommand(String orderId) {
    public static PayOrderCommand with(final String orderId) {
        return new PayOrderCommand(orderId);
    }
}
