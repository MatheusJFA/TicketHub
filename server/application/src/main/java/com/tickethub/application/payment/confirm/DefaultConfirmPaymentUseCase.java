package com.tickethub.application.payment.confirm;

import static java.util.Objects.requireNonNull;

import java.time.Clock;

import com.tickethub.application.Either;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderStatus;
import com.tickethub.domain.core.payment.ChargeID;
import com.tickethub.domain.core.payment.ChargeStatus;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.domain.core.ticket.Ticket;
import com.tickethub.domain.core.ticket.TicketGateway;
import com.tickethub.domain.core.ticket.TicketSigner;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.exception.OrderExpiredException;
import com.tickethub.domain.validation.Notification;

/**
 * Handles the provider webhook: links the charge to its order and, on PAID,
 * settles the order and issues one ticket per item. Retried webhooks for an
 * already PAID order are idempotent. FAILED charges leave the order PENDING
 * until the buyer retries or the reservation expires.
 */
public class DefaultConfirmPaymentUseCase extends ConfirmPaymentUseCase {
    private final OrderGateway orderGateway;
    private final TicketGateway ticketGateway;
    private final TicketSigner ticketSigner;
    private final PaymentGateway paymentGateway;
    private final Clock clock;

    public DefaultConfirmPaymentUseCase(final OrderGateway orderGateway, final TicketGateway ticketGateway,
            final TicketSigner ticketSigner, final PaymentGateway paymentGateway) {
        this(orderGateway, ticketGateway, ticketSigner, paymentGateway, Clock.systemUTC());
    }

    public DefaultConfirmPaymentUseCase(final OrderGateway orderGateway, final TicketGateway ticketGateway,
            final TicketSigner ticketSigner, final PaymentGateway paymentGateway, final Clock clock) {
        this.orderGateway = requireNonNull(orderGateway, "'orderGateway' should not be null");
        this.ticketGateway = requireNonNull(ticketGateway, "'ticketGateway' should not be null");
        this.ticketSigner = requireNonNull(ticketSigner, "'ticketSigner' should not be null");
        this.paymentGateway = requireNonNull(paymentGateway, "'paymentGateway' should not be null");
        this.clock = requireNonNull(clock, "'clock' should not be null");
    }

    @Override
    public Either<Notification, ConfirmPaymentOutput> execute(final ConfirmPaymentCommand command) {
        try {
            final ChargeStatus status;
            try {
                status = ChargeStatus.valueOf(command.status());
            } catch (final IllegalArgumentException | NullPointerException invalid) {
                return Either.left(Notification.create(
                        new DomainException("Invalid charge status: " + command.status())));
            }
            final var found = orderGateway.findByChargeId(ChargeID.from(command.chargeId()));
            if (found.isEmpty()) {
                return Either.left(notFound("Charge", command.chargeId()));
            }
            final Order order = found.get();
            if (status == ChargeStatus.FAILED || order.getStatus() == OrderStatus.PAID) {
                return Either.right(ConfirmPaymentOutput.from(order));
            }
            // Reconcile with the provider and drive the charge state machine:
            // only a PENDING charge accepts the webhook status.
            paymentGateway.findStatus(order.getChargeId()).changeStatus(status);
            if (order.expireIfElapsed(clock.instant())) {
                orderGateway.update(order);
                return Either.left(Notification.create(new OrderExpiredException()));
            }
            order.markAsPaid(clock);
            for (final var item : order.getItems()) {
                ticketGateway.create(Ticket.issue(order.getId(), item.getSpotId(),
                        order.getCustomerId(), ticketSigner));
            }
            return Either.right(ConfirmPaymentOutput.from(orderGateway.update(order)));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
