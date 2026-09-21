package com.tickethub.application.payment.pay;

import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.payment.Charge;

public record PayOrderOutput(
        String orderId,
        String chargeId,
        String paymentCode,
        String chargeStatus) {
    public static PayOrderOutput from(final Order order, final Charge charge) {
        return new PayOrderOutput(
                order.getId().getValue(),
                charge.getChargeId().getValue(),
                charge.getPaymentCode(),
                charge.getStatus().name());
    }
}
