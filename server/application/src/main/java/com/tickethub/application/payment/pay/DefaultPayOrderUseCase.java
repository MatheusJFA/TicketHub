package com.tickethub.application.payment.pay;

import static java.util.Objects.requireNonNull;

import java.time.Clock;

import com.tickethub.application.Either;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.domain.exception.OrderExpiredException;
import com.tickethub.domain.validation.Notification;

/**
 * Starts payment for a PENDING order by creating a provider charge. Retrying
 * with an already charged order is idempotent and returns the current charge
 * instead of billing twice. Expired reservations are finalized here so the
 * sweeper never leaves a stale PENDING order behind.
 */
public class DefaultPayOrderUseCase extends PayOrderUseCase {
    private final OrderGateway orderGateway;
    private final PaymentGateway paymentGateway;
    private final Clock clock;

    public DefaultPayOrderUseCase(final OrderGateway orderGateway, final PaymentGateway paymentGateway) {
        this(orderGateway, paymentGateway, Clock.systemUTC());
    }

    public DefaultPayOrderUseCase(final OrderGateway orderGateway, final PaymentGateway paymentGateway,
            final Clock clock) {
        this.orderGateway = requireNonNull(orderGateway, "'orderGateway' should not be null");
        this.paymentGateway = requireNonNull(paymentGateway, "'paymentGateway' should not be null");
        this.clock = requireNonNull(clock, "'clock' should not be null");
    }

    @Override
    public Either<Notification, PayOrderOutput> execute(final PayOrderCommand command) {
        try {
            final var orderId = OrderID.from(command.orderId());
            final var found = orderGateway.findById(orderId);
            if (found.isEmpty()) {
                return Either.left(notFound("Order", orderId.getValue()));
            }
            final Order order = found.get();
            if (order.hasCharge()) {
                final var current = paymentGateway.findStatus(order.getChargeId());
                return Either.right(PayOrderOutput.from(order, current));
            }
            if (order.expireIfElapsed(clock.instant())) {
                orderGateway.update(order);
                return Either.left(Notification.create(new OrderExpiredException()));
            }
            final var charge = paymentGateway.createCharge(order.getId(), order.getTotal());
            order.attachCharge(charge.getChargeId());
            return Either.right(PayOrderOutput.from(orderGateway.update(order), charge));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
