package com.tickethub.application.order.pay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.order.OrderItem;
import com.tickethub.domain.core.payment.Charge;
import com.tickethub.domain.core.payment.ChargeStatus;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.shared.Money;

@DisplayName("Pay order use case")
class PayOrderUseCaseTest extends UseCaseTest {

    private static final Currency BRL = Currency.getInstance("BRL");
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-20T12:00:00Z"), ZoneOffset.UTC);

    private final OrderGateway orderGateway = mock(OrderGateway.class);
    private final PaymentGateway paymentGateway = mock(PaymentGateway.class);
    private final DefaultPayOrderUseCase useCase =
            new DefaultPayOrderUseCase(orderGateway, paymentGateway, CLOCK);

    @Override
    protected List<Object> getMocks() {
        return List.of(orderGateway, paymentGateway);
    }

    private Order givenOrder() {
        return Order.create(CustomerID.generate(),
                List.of(OrderItem.of(SpotID.generate(), Money.create(new BigDecimal("50.00"), BRL))),
                TTL, CLOCK);
    }

    private Charge givenCharge(final Order order) {
        return new Charge("ch_123", order.getId(), order.getTotal(), ChargeStatus.PENDING,
                "PIX-MOCK-ch_123");
    }

    @Test
    @DisplayName("Given pending order, when execute, then creates charge and links it")
    void givenPendingOrder_whenExecute_thenCreatesCharge() {
        final var order = givenOrder();
        final var charge = givenCharge(order);
        when(orderGateway.findById(order.getId())).thenReturn(Optional.of(order));
        when(paymentGateway.createCharge(order.getId(), order.getTotal())).thenReturn(charge);
        when(orderGateway.update(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(PayOrderCommand.with(order.getId().getValue())).getRight();

        assertNotNull(output);
        assertEquals(order.getId().getValue(), output.orderId());
        assertEquals("ch_123", output.chargeId());
        assertEquals("PIX-MOCK-ch_123", output.paymentCode());
        assertEquals(ChargeStatus.PENDING.name(), output.chargeStatus());
        verify(orderGateway, times(1)).findById(order.getId());
        verify(paymentGateway, times(1)).createCharge(order.getId(), order.getTotal());
        verify(orderGateway, times(1)).update(any());
    }

    @Test
    @DisplayName("Given already charged order, when execute, then returns current charge without billing twice")
    void givenChargedOrder_whenExecute_thenReturnsCurrentCharge() {
        final var order = givenOrder();
        final var charge = givenCharge(order);
        order.attachCharge(charge.chargeId());
        when(orderGateway.findById(order.getId())).thenReturn(Optional.of(order));
        when(paymentGateway.findStatus("ch_123")).thenReturn(charge);

        final var output = useCase.execute(PayOrderCommand.with(order.getId().getValue())).getRight();

        assertEquals("ch_123", output.chargeId());
        verify(orderGateway, times(1)).findById(order.getId());
        verify(paymentGateway, times(0)).createCharge(any(), any());
        verify(paymentGateway, times(1)).findStatus("ch_123");
    }

    @Test
    @DisplayName("Given unknown order, when execute, then returns not found")
    void givenUnknownOrder_whenExecute_thenReturnsNotFound() {
        final var orderId = OrderID.generate();
        when(orderGateway.findById(orderId)).thenReturn(Optional.empty());

        final var notification = useCase.execute(PayOrderCommand.with(orderId.getValue())).getLeft();

        assertEquals("Order not found: " + orderId.getValue(), notification.firstError().message());
        verify(orderGateway, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("Given expired reservation, when execute, then expires order and refuses payment")
    void givenExpiredOrder_whenExecute_thenExpiresAndRefuses() {
        final var order = givenOrder();
        when(orderGateway.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderGateway.update(any())).thenAnswer(returnsFirstArg());
        final var late = new DefaultPayOrderUseCase(orderGateway, paymentGateway,
                Clock.fixed(CLOCK.instant().plus(TTL).plusSeconds(1), ZoneOffset.UTC));

        final var notification =
                late.execute(PayOrderCommand.with(order.getId().getValue())).getLeft();

        assertEquals("Order is expired", notification.firstError().message());
        verify(orderGateway, times(1)).findById(order.getId());
        verify(paymentGateway, times(0)).createCharge(any(), any());
        verify(orderGateway, times(1)).update(any());
    }
}
