package com.tickethub.domain.core.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;

import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.exception.IllegalOrderTransitionException;
import com.tickethub.domain.exception.OrderExpiredException;
import com.tickethub.domain.shared.Money;
import com.tickethub.domain.validation.Notification;

@DisplayName("Order")
class OrderTest {

    private static final Currency BRL = Currency.getInstance("BRL");
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-20T12:00:00Z"), ZoneOffset.UTC);

    private static List<OrderItem> items() {
        return List.of(
                OrderItem.of(SpotID.generate(), Money.create(new BigDecimal("50.00"), BRL)),
                OrderItem.of(SpotID.generate(), Money.create(new BigDecimal("25.50"), BRL)));
    }

    @Test
    @DisplayName("Given customer and items, when create, then open pending order with summed total")
    void givenCustomerAndItems_whenCreate_thenOpenPendingOrder() {
        final var customerId = CustomerID.generate();

        final var order = Order.create(customerId, items(), TTL, CLOCK);

        assertNotNull(order.getId());
        assertEquals(customerId, order.getCustomerId());
        assertEquals(2, order.getItems().size());
        assertEquals(Money.create(new BigDecimal("75.50"), BRL), order.getTotal());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(CLOCK.instant().plus(TTL), order.getExpiresAt());
        assertNull(order.getChargeId());
        assertFalse(order.hasCharge());
        assertEquals(1, order.domainEvents().size());
        assertTrue(order.domainEvents().get(0) instanceof OrderCreated);
    }

    @Test
    @DisplayName("Given no items, when create, then throw domain exception")
    void givenNoItems_whenCreate_thenThrowDomainException() {
        final var exception = assertThrows(DomainException.class,
                () -> Order.create(CustomerID.generate(), List.of(), TTL, CLOCK));

        assertEquals("'items' should not be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Given items with mixed currencies, when create, then throw domain exception")
    void givenMixedCurrencies_whenCreate_thenThrowDomainException() {
        final var mixed = List.of(
                OrderItem.of(SpotID.generate(), Money.create(BigDecimal.TEN, BRL)),
                OrderItem.of(SpotID.generate(),
                        Money.create(BigDecimal.TEN, Currency.getInstance("USD"))));

        assertThrows(DomainException.class,
                () -> Order.create(CustomerID.generate(), mixed, TTL, CLOCK));
    }

    @Test
    @DisplayName("Given pending order, when attach charge, then link charge id")
    void givenPendingOrder_whenAttachCharge_thenLinkChargeId() {
        final var order = Order.create(CustomerID.generate(), items(), TTL, CLOCK);

        order.attachCharge("ch_123");

        assertEquals("ch_123", order.getChargeId());
        assertTrue(order.hasCharge());
    }

    @Test
    @DisplayName("Given paid order, when attach charge, then throw illegal transition")
    void givenPaidOrder_whenAttachCharge_thenThrowIllegalTransition() {
        final var order = Order.create(CustomerID.generate(), items(), TTL, CLOCK);
        order.markAsPaid(CLOCK);

        final var exception = assertThrows(IllegalOrderTransitionException.class,
                () -> order.attachCharge("ch_123"));

        assertEquals("Illegal order transition from PAID to PENDING", exception.getMessage());
    }

    @Test
    @DisplayName("Given pending order, when mark as paid, then status paid with event")
    void givenPendingOrder_whenMarkAsPaid_thenStatusPaid() {
        final var order = Order.create(CustomerID.generate(), items(), TTL, CLOCK);

        order.markAsPaid(CLOCK);

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(2, order.domainEvents().size());
        assertTrue(order.domainEvents().get(1) instanceof OrderPaid);
    }

    @Test
    @DisplayName("Given paid order, when mark as paid again, then throw illegal transition")
    void givenPaidOrder_whenMarkAsPaidAgain_thenThrowIllegalTransition() {
        final var order = Order.create(CustomerID.generate(), items(), TTL, CLOCK);
        order.markAsPaid(CLOCK);

        final var exception = assertThrows(IllegalOrderTransitionException.class,
                () -> order.markAsPaid(CLOCK));

        assertEquals("Illegal order transition from PAID to PAID", exception.getMessage());
    }

    @Test
    @DisplayName("Given expired reservation, when mark as paid, then expire and throw expired")
    void givenExpiredReservation_whenMarkAsPaid_thenExpireAndThrow() {
        final var order = Order.create(CustomerID.generate(), items(), TTL, CLOCK);
        final var late = Clock.fixed(CLOCK.instant().plus(TTL).plusSeconds(1), ZoneOffset.UTC);

        final var exception = assertThrows(OrderExpiredException.class,
                () -> order.markAsPaid(late));

        assertEquals("Order is expired", exception.getMessage());
        assertEquals(OrderStatus.EXPIRED, order.getStatus());
    }

    @Test
    @DisplayName("Given pending order, when cancel, then status cancelled with event")
    void givenPendingOrder_whenCancel_thenStatusCancelled() {
        final var order = Order.create(CustomerID.generate(), items(), TTL, CLOCK);

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertTrue(order.domainEvents().get(1) instanceof OrderCancelled);
    }

    @Test
    @DisplayName("Given paid order, when cancel, then throw illegal transition")
    void givenPaidOrder_whenCancel_thenThrowIllegalTransition() {
        final var order = Order.create(CustomerID.generate(), items(), TTL, CLOCK);
        order.markAsPaid(CLOCK);

        final var exception = assertThrows(IllegalOrderTransitionException.class, order::cancel);

        assertEquals("Illegal order transition from PAID to CANCELLED", exception.getMessage());
    }

    @Test
    @DisplayName("Given cancelled order, when cancel again, then stay cancelled without new event")
    void givenCancelledOrder_whenCancelAgain_thenStayCancelled() {
        final var order = Order.create(CustomerID.generate(), items(), TTL, CLOCK);
        order.cancel();
        final var events = order.domainEvents().size();

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(events, order.domainEvents().size());
    }

    @Test
    @DisplayName("Given elapsed TTL, when expire if elapsed, then status expired")
    void givenElapsedTtl_whenExpireIfElapsed_thenStatusExpired() {
        final var order = Order.create(CustomerID.generate(), items(), TTL, CLOCK);

        assertFalse(order.expireIfElapsed(CLOCK.instant()));
        assertEquals(OrderStatus.PENDING, order.getStatus());

        assertTrue(order.expireIfElapsed(CLOCK.instant().plus(TTL)));
        assertEquals(OrderStatus.EXPIRED, order.getStatus());
        assertTrue(order.domainEvents().get(1) instanceof OrderExpired);
    }

    @Test
    @DisplayName("Given valid order, when validate, then no errors")
    void givenValidOrder_whenValidate_thenNoErrors() {
        final var order = Order.create(CustomerID.generate(), items(), TTL, CLOCK);

        final var notification = Notification.create();
        order.validate(notification);

        assertTrue(notification.getErrors().isEmpty());
    }
}
