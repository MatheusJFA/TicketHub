package com.tickethub.infrastructure.payment.models;

import com.tickethub.application.payment.confirm.ConfirmPaymentOutput;

public record ConfirmPaymentResponse(String orderId, String orderStatus) {
    public static ConfirmPaymentResponse from(final ConfirmPaymentOutput output) {
        return new ConfirmPaymentResponse(output.orderId(), output.orderStatus());
    }
}
