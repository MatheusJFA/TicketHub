package com.tickethub.application.payment.pay;

public record PayOrderCommand(String orderId) {
    public static PayOrderCommand with(final String orderId) {
        return new PayOrderCommand(orderId);
    }
}
