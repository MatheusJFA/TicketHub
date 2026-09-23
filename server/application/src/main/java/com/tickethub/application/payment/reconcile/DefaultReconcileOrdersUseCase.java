package com.tickethub.application.payment.reconcile;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.application.sales.SaleRecorder;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.payment.ChargeStatus;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.ticket.Ticket;
import com.tickethub.domain.core.ticket.TicketGateway;
import com.tickethub.domain.core.ticket.TicketSigner;
import com.tickethub.domain.validation.Notification;

/**
 * Reconciles PENDING orders past their TTL that already have a charge —
 * money in flight whose webhook never arrived. Approvals inside the
 * reservation TTL settle (tickets issued); late approvals refund; anything
 * else expires. Orders without charge belong to the expiry sweeper.
 * Per-order failures are skipped and retried on the next run.
 */
public class DefaultReconcileOrdersUseCase extends ReconcileOrdersUseCase {
    private final OrderGateway orderGateway;
    private final TicketGateway ticketGateway;
    private final TicketSigner ticketSigner;
    private final PaymentGateway paymentGateway;
    private final SpotGateway spotGateway;
    private final SaleRecorder sales;
    private final Clock clock;

    public DefaultReconcileOrdersUseCase(final OrderGateway orderGateway, final TicketGateway ticketGateway,
            final TicketSigner ticketSigner, final PaymentGateway paymentGateway,
            final SpotGateway spotGateway, final SaleRecorder sales) {
        this(orderGateway, ticketGateway, ticketSigner, paymentGateway, spotGateway, sales, Clock.systemUTC());
    }

    public DefaultReconcileOrdersUseCase(final OrderGateway orderGateway, final TicketGateway ticketGateway,
            final TicketSigner ticketSigner, final PaymentGateway paymentGateway,
            final SpotGateway spotGateway, final SaleRecorder sales, final Clock clock) {
        this.orderGateway = requireNonNull(orderGateway, "'orderGateway' should not be null");
        this.ticketGateway = requireNonNull(ticketGateway, "'ticketGateway' should not be null");
        this.ticketSigner = requireNonNull(ticketSigner, "'ticketSigner' should not be null");
        this.paymentGateway = requireNonNull(paymentGateway, "'paymentGateway' should not be null");
        this.spotGateway = requireNonNull(spotGateway, "'spotGateway' should not be null");
        this.sales = requireNonNull(sales, "'sales' should not be null");
        this.clock = requireNonNull(clock, "'clock' should not be null");
    }

    @Override
    public Either<Notification, ReconcileOrdersOutput> execute() {
        try {
            final var now = clock.instant();
            final var settled = new ArrayList<String>();
            final var refunded = new ArrayList<String>();
            final var expired = new ArrayList<String>();
            for (final Order order : orderGateway.findPendingExpired(now)) {
                if (!order.hasCharge()) {
                    continue;
                }
                try {
                    reconcile(order, now, settled, refunded, expired);
                } catch (final RuntimeException ignored) {
                    // Best effort per order: the next sweep retries what failed here.
                }
            }
            return Either.right(ReconcileOrdersOutput.from(settled, refunded, expired));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }

    private void reconcile(final Order order, final Instant now, final List<String> settled,
            final List<String> refunded, final List<String> expired) {
        final var charge = paymentGateway.findStatus(order.getChargeId());
        if (charge.getStatus() != ChargeStatus.PAID) {
            if (order.expireIfElapsed(now)) {
                releaseSpots(order);
                orderGateway.update(order);
                expired.add(order.getId().getValue());
            }
            return;
        }
        final var approvedAt = Optional.ofNullable(charge.getApprovedAt()).orElse(now);
        if (!approvedAt.isAfter(order.getExpiresAt())) {
            order.markAsPaidAt(approvedAt, clock);
            for (final var item : order.getItems()) {
                ticketGateway.create(Ticket.issue(order.getId(), item.getSpotId(),
                        order.getCustomerId(), ticketSigner));
            }
            sales.recordSale(order);
            orderGateway.update(order);
            settled.add(order.getId().getValue());
            return;
        }
        paymentGateway.refund(order.getChargeId());
        order.expireIfElapsed(now);
        order.refund();
        sales.recordRefund(order);
        releaseSpots(order);
        orderGateway.update(order);
        refunded.add(order.getId().getValue());
    }

    private void releaseSpots(final Order order) {
        for (final var item : order.getItems()) {
            spotGateway.findById(item.getSpotId()).ifPresent(spot -> {
                spot.release();
                spotGateway.update(spot);
            });
        }
    }
}
