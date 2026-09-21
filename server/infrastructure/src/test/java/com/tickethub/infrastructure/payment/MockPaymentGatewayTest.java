package com.tickethub.infrastructure.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.payment.ChargeStatus;
import com.tickethub.domain.exception.ResourceNotFoundException;
import com.tickethub.domain.shared.Money;

@DisplayName("Mock payment gateway")
class MockPaymentGatewayTest {

    private final MockPaymentGateway gateway = new MockPaymentGateway();

    @Test
    @DisplayName("Given order total, when creates charge, then returns pending charge with payment code")
    void givenOrderTotal_whenCreatesCharge_thenReturnsPendingCharge() {
        final var orderId = OrderID.generate();
        final var total = Money.create(new BigDecimal("75.50"), Currency.getInstance("BRL"));

        final var charge = gateway.createCharge(orderId, total);

        assertNotNull(charge.chargeId());
        assertEquals(orderId, charge.orderId());
        assertEquals(total, charge.total());
        assertEquals(ChargeStatus.PENDING, charge.status());
        assertNotNull(charge.paymentCode());
    }

    @Test
    @DisplayName("Given created charge, when finds status, then returns stored charge")
    void givenCreatedCharge_whenFindsStatus_thenReturnsStored() {
        final var created = gateway.createCharge(OrderID.generate(),
                Money.create(BigDecimal.TEN, Currency.getInstance("BRL")));

        assertEquals(created, gateway.findStatus(created.chargeId()));
    }

    @Test
    @DisplayName("Given unknown charge, when finds status, then throws not found")
    void givenUnknownCharge_whenFindsStatus_thenThrowsNotFound() {
        final var exception = assertThrows(ResourceNotFoundException.class,
                () -> gateway.findStatus("ch_missing"));

        assertEquals("Charge not found: ch_missing", exception.getMessage());
    }
}
