package com.tickethub.application.payment.reconcile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.tickethub.application.sales.SaleRecorder;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderItem;
import com.tickethub.domain.core.order.OrderStatus;
import com.tickethub.domain.core.payment.Charge;
import com.tickethub.domain.core.payment.ChargeID;
import com.tickethub.domain.core.payment.ChargeStatus;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.core.ticket.TicketGateway;
import com.tickethub.domain.core.ticket.TicketSigner;
import com.tickethub.domain.shared.Location;
import com.tickethub.domain.shared.Money;

@DisplayName("Reconcile orders use case")
class ReconcileOrdersUseCaseTest extends UseCaseTest {

    private static final Currency BRL = Currency.getInstance("BRL");
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-20T12:00:00Z"), ZoneOffset.UTC);
    private static final Clock PAST = Clock.fixed(CLOCK.instant().minus(TTL).minusSeconds(60), ZoneOffset.UTC);
    private static final TicketSigner SIGNER = payload -> "signed:" + payload;

    private final OrderGateway orderGateway = mock(OrderGateway.class);
    private final TicketGateway ticketGateway = mock(TicketGateway.class);
    private final PaymentGateway paymentGateway = mock(PaymentGateway.class);
    private final SpotGateway spotGateway = mock(SpotGateway.class);
    private final SaleRecorder sales = mock(SaleRecorder.class);
    private final DefaultReconcileOrdersUseCase useCase = new DefaultReconcileOrdersUseCase(
            orderGateway, ticketGateway, SIGNER, paymentGateway, spotGateway, sales, CLOCK);

    @Override
    protected List<Object> getMocks() {
        return List.of(orderGateway, ticketGateway, paymentGateway, spotGateway, sales);
    }

    private Order givenChargedOrder(final String charge) {
        final var spot = Spot.create(Location.create("A1"));
        final var order = Order.create(CustomerID.generate(),
                List.of(OrderItem.of(spot.getId(), Money.create(new BigDecimal("50.00"), BRL))),
                TTL, PAST);
        order.attachCharge(ChargeID.from(charge));
        when(spotGateway.findById(spot.getId())).thenReturn(Optional.of(spot));
        when(spotGateway.update(any())).thenAnswer(returnsFirstArg());
        when(ticketGateway.create(any())).thenAnswer(returnsFirstArg());
        when(orderGateway.update(any())).thenAnswer(returnsFirstArg());
        return order;
    }

    private Charge providerCharge(final Order order, final ChargeStatus status, final Instant approvedAt) {
        return Charge.create(order.getChargeId(), order.getId(), order.getTotal(), status,
                "PIX", approvedAt);
    }

    @Test
    @DisplayName("Given in-time approval, when execute, then settles and issues tickets")
    void givenInTimeApproval_whenExecute_thenSettles() {
        final var order = givenChargedOrder("ch_1");
        when(orderGateway.findPendingExpired(any())).thenReturn(List.of(order));
        when(paymentGateway.findStatus(order.getChargeId()))
                .thenReturn(providerCharge(order, ChargeStatus.PAID, PAST.instant().plus(Duration.ofMinutes(5))));

        final var output = useCase.execute().getRight();

        assertEquals(List.of(order.getId().getValue()), output.settled());
        verify(orderGateway, times(1)).findPendingExpired(any());
        assertTrue(output.refunded().isEmpty());
        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(paymentGateway, times(1)).findStatus(order.getChargeId());
        verify(ticketGateway, times(1)).create(any());
        verify(sales, times(1)).recordSale(any());
        verify(sales, times(0)).recordRefund(any());
        verify(orderGateway, times(1)).update(any());
        verify(spotGateway, times(0)).update(any());
    }

    @Test
    @DisplayName("Given late approval, when execute, then refunds and releases spots")
    void givenLateApproval_whenExecute_thenRefunds() {
        final var order = givenChargedOrder("ch_2");
        when(orderGateway.findPendingExpired(any())).thenReturn(List.of(order));
        when(paymentGateway.findStatus(order.getChargeId()))
                .thenReturn(providerCharge(order, ChargeStatus.PAID,
                        PAST.instant().plus(TTL).plusSeconds(1)));

        final var output = useCase.execute().getRight();

        assertTrue(output.settled().isEmpty());
        assertEquals(List.of(order.getId().getValue()), output.refunded());
        verify(orderGateway, times(1)).findPendingExpired(any());
        assertEquals(OrderStatus.REFUNDED, order.getStatus());
        verify(paymentGateway, times(1)).findStatus(order.getChargeId());
        verify(paymentGateway, times(1)).refund(order.getChargeId());
        verify(sales, times(0)).recordSale(any());
        verify(sales, times(1)).recordRefund(any());
        verify(ticketGateway, times(0)).create(any());
        verify(spotGateway, times(1)).findById(order.getItems().get(0).getSpotId());
        verify(spotGateway, times(1)).update(any());
        verify(orderGateway, times(1)).update(any());
    }

    @Test
    @DisplayName("Given failed charge, when execute, then expires without refund")
    void givenFailedCharge_whenExecute_thenExpires() {
        final var order = givenChargedOrder("ch_3");
        when(orderGateway.findPendingExpired(any())).thenReturn(List.of(order));
        when(paymentGateway.findStatus(order.getChargeId()))
                .thenReturn(providerCharge(order, ChargeStatus.FAILED, null));

        final var output = useCase.execute().getRight();

        assertTrue(output.settled().isEmpty());
        assertTrue(output.refunded().isEmpty());
        assertEquals(List.of(order.getId().getValue()), output.expired());
        verify(orderGateway, times(1)).findPendingExpired(any());
        assertEquals(OrderStatus.EXPIRED, order.getStatus());
        verify(paymentGateway, times(1)).findStatus(order.getChargeId());
        verify(spotGateway, times(1)).findById(order.getItems().get(0).getSpotId());
        verify(spotGateway, times(1)).update(any());
        verify(paymentGateway, times(0)).refund(any());
        verify(ticketGateway, times(0)).create(any());
        verify(orderGateway, times(1)).update(any());
    }

    @Test
    @DisplayName("Given order without charge, when execute, then skips for sweeper")
    void givenOrderWithoutCharge_whenExecute_thenSkips() {
        final var plain = Order.create(CustomerID.generate(),
                List.of(OrderItem.of(SpotID.generate(), Money.create(new BigDecimal("50.00"), BRL))),
                TTL, CLOCK);
        when(orderGateway.findPendingExpired(any())).thenReturn(List.of(plain));

        final var output = useCase.execute().getRight();

        assertTrue(output.isEmpty());
        verify(orderGateway, times(1)).findPendingExpired(any());
        verify(paymentGateway, times(0)).findStatus(any());
        verify(orderGateway, times(0)).update(any());
    }
}
