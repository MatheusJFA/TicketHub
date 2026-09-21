package com.tickethub.application.payment.confirm;

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
import com.tickethub.domain.core.order.OrderItem;
import com.tickethub.domain.core.order.OrderStatus;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.core.ticket.TicketGateway;
import com.tickethub.domain.core.ticket.TicketSigner;
import com.tickethub.domain.shared.Money;

@DisplayName("Confirm payment use case")
class ConfirmPaymentUseCaseTest extends UseCaseTest {

    private static final Currency BRL = Currency.getInstance("BRL");
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-20T12:00:00Z"), ZoneOffset.UTC);
    private static final TicketSigner SIGNER = payload -> "signed:" + payload;

    private final OrderGateway orderGateway = mock(OrderGateway.class);
    private final TicketGateway ticketGateway = mock(TicketGateway.class);
    private final DefaultConfirmPaymentUseCase useCase =
            new DefaultConfirmPaymentUseCase(orderGateway, ticketGateway, SIGNER, CLOCK);

    @Override
    protected List<Object> getMocks() {
        return List.of(orderGateway, ticketGateway);
    }

    private Order givenChargedOrder() {
        final var order = Order.create(CustomerID.generate(),
                List.of(
                        OrderItem.of(SpotID.generate(), Money.create(new BigDecimal("50.00"), BRL)),
                        OrderItem.of(SpotID.generate(), Money.create(new BigDecimal("25.00"), BRL))),
                TTL, CLOCK);
        order.attachCharge("ch_123");
        when(orderGateway.findByChargeId("ch_123")).thenReturn(Optional.of(order));
        when(ticketGateway.create(any())).thenAnswer(returnsFirstArg());
        when(orderGateway.update(any())).thenAnswer(returnsFirstArg());
        return order;
    }

    @Test
    @DisplayName("Given paid charge, when execute, then settles order and issues tickets")
    void givenPaidCharge_whenExecute_thenSettlesAndIssuesTickets() {
        final var order = givenChargedOrder();

        final var output = useCase.execute(ConfirmPaymentCommand.with("ch_123", "PAID")).getRight();

        assertNotNull(output);
        assertEquals(order.getId().getValue(), output.orderId());
        assertEquals(OrderStatus.PAID.name(), output.orderStatus());
        verify(orderGateway, times(1)).findByChargeId("ch_123");
        verify(ticketGateway, times(2)).create(any());
        verify(orderGateway, times(1)).update(any());
    }

    @Test
    @DisplayName("Given failed charge, when execute, then keeps order pending without tickets")
    void givenFailedCharge_whenExecute_thenKeepsPending() {
        final var order = givenChargedOrder();

        final var output = useCase.execute(ConfirmPaymentCommand.with("ch_123", "FAILED")).getRight();

        assertEquals(OrderStatus.PENDING.name(), output.orderStatus());
        verify(orderGateway, times(1)).findByChargeId("ch_123");
        verify(ticketGateway, times(0)).create(any());
        verify(orderGateway, times(0)).update(any());
    }

    @Test
    @DisplayName("Given unknown charge, when execute, then returns not found")
    void givenUnknownCharge_whenExecute_thenReturnsNotFound() {
        when(orderGateway.findByChargeId("ch_missing")).thenReturn(Optional.empty());

        final var notification =
                useCase.execute(ConfirmPaymentCommand.with("ch_missing", "PAID")).getLeft();

        assertEquals("Charge not found: ch_missing", notification.firstError().message());
        verify(orderGateway, times(1)).findByChargeId("ch_missing");
    }

    @Test
    @DisplayName("Given invalid status, when execute, then returns validation error")
    void givenInvalidStatus_whenExecute_thenReturnsError() {
        final var notification =
                useCase.execute(ConfirmPaymentCommand.with("ch_123", "BOGUS")).getLeft();

        assertEquals("Invalid charge status: BOGUS", notification.firstError().message());
    }

    @Test
    @DisplayName("Given already paid order, when webhook repeats, then stays paid without new tickets")
    void givenPaidOrder_whenWebhookRepeats_thenIdempotent() {
        final var order = givenChargedOrder();
        order.markAsPaid(CLOCK);

        final var output = useCase.execute(ConfirmPaymentCommand.with("ch_123", "PAID")).getRight();

        assertEquals(OrderStatus.PAID.name(), output.orderStatus());
        verify(orderGateway, times(1)).findByChargeId("ch_123");
        verify(ticketGateway, times(0)).create(any());
    }
}
