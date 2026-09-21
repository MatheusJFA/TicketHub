package com.tickethub.application.order.cancel;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.validation.Notification;

/**
 * Cancels an open order at the buyer's request and releases its spots back
 * on sale. Paid orders cannot be cancelled in this version (no refunds yet).
 */
public class DefaultCancelOrderUseCase extends CancelOrderUseCase {
    private final OrderGateway orderGateway;
    private final SpotGateway spotGateway;

    public DefaultCancelOrderUseCase(final OrderGateway orderGateway, final SpotGateway spotGateway) {
        this.orderGateway = requireNonNull(orderGateway, "'orderGateway' should not be null");
        this.spotGateway = requireNonNull(spotGateway, "'spotGateway' should not be null");
    }

    @Override
    public Either<Notification, CancelOrderOutput> execute(final CancelOrderCommand command) {
        try {
            final var orderId = OrderID.from(command.orderId());
            final var found = orderGateway.findById(orderId);
            if (found.isEmpty()) {
                return Either.left(notFound("Order", orderId.getValue()));
            }
            final Order order = found.get();
            order.cancel();
            for (final var item : order.getItems()) {
                spotGateway.findById(item.getSpotId()).ifPresent(spot -> {
                    spot.release();
                    spotGateway.update(spot);
                });
            }
            return Either.right(CancelOrderOutput.from(orderGateway.update(order)));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
