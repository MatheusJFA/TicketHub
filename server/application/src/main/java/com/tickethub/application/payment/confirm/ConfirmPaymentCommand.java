package com.tickethub.application.payment.confirm;

public record ConfirmPaymentCommand(String chargeId, String status) {
    public static ConfirmPaymentCommand with(final String chargeId, final String status) {
        return new ConfirmPaymentCommand(chargeId, status);
    }
}
