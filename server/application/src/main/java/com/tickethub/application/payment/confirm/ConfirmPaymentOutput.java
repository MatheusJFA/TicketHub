package com.tickethub.application.payment.confirm;

import com.tickethub.domain.core.order.Order;

public record ConfirmPaymentOutput(String orderId, String orderStatus) {
    public static ConfirmPaymentOutput from(final Order order) {
        return new ConfirmPaymentOutput(
                order.getId().getValue(),
                order.getStatus().name());
    }
}
