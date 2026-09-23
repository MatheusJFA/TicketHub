package com.tickethub.application.order.retrieve.get;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.validation.Notification;

public class DefaultGetOrderUseCase extends GetOrderUseCase {
    private final OrderGateway orderGateway;

    public DefaultGetOrderUseCase(final OrderGateway orderGateway) {
        this.orderGateway = requireNonNull(orderGateway, "'orderGateway' should not be null");
    }

    @Override
    public Either<Notification, GetOrderOutput> execute(final String orderId) {
        try {
            final var id = OrderID.from(orderId);
            return findOrNotFound(orderGateway.findById(id).map(GetOrderOutput::from), "Order", id.getValue());
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
