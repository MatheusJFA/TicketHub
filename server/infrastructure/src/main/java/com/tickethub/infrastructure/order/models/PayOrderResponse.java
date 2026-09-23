package com.tickethub.infrastructure.order.models;

import com.tickethub.application.payment.pay.PayOrderOutput;

public record PayOrderResponse(String orderId, String chargeId, String paymentCode, String chargeStatus) {
    public static PayOrderResponse from(final PayOrderOutput output) {
        return new PayOrderResponse(output.orderId(), output.chargeId(), output.paymentCode(), output.chargeStatus());
    }
}
