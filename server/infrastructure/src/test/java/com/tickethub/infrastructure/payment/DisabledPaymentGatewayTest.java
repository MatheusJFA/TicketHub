package com.tickethub.infrastructure.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.payment.ChargeID;
import com.tickethub.domain.shared.Money;
import com.tickethub.infrastructure.exception.InfrastructureException;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Disabled payment gateway")
class DisabledPaymentGatewayTest {

    private final DisabledPaymentGateway gateway = new DisabledPaymentGateway();

    @Test
    @DisplayName("Create charge fails closed with 503 mapping")
    void createCharge_failsClosed() {
        final var exception = assertThrows(
                InfrastructureException.class,
                () -> gateway.createCharge(
                        OrderID.generate(), Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"))));
        assertEquals("Payment provider is not configured: set MERCADOPAGO_ACCESS_TOKEN", exception.getMessage());
    }

    @Test
    @DisplayName("Find status fails closed")
    void findStatus_failsClosed() {
        assertThrows(InfrastructureException.class, () -> gateway.findStatus(ChargeID.generate()));
    }

    @Test
    @DisplayName("Refund fails closed")
    void refund_failsClosed() {
        assertThrows(InfrastructureException.class, () -> gateway.refund(ChargeID.generate()));
    }
}
