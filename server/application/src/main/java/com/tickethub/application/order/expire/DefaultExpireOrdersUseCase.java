package com.tickethub.application.order.expire;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.ArrayList;

import com.tickethub.application.Either;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.validation.Notification;

/**
 * Sweeps PENDING orders past their reservation TTL, expires them and
 * releases their spots back on sale. Runs on a schedule (and doubles as a
 * safety net for paths that finalize expiry lazily, like payment).
 */
public class DefaultExpireOrdersUseCase extends ExpireOrdersUseCase {
    private final OrderGateway orderGateway;
    private final SpotGateway spotGateway;
    private final Clock clock;

    public DefaultExpireOrdersUseCase(final OrderGateway orderGateway, final SpotGateway spotGateway) {
        this(orderGateway, spotGateway, Clock.systemUTC());
    }

    public DefaultExpireOrdersUseCase(final OrderGateway orderGateway, final SpotGateway spotGateway,
            final Clock clock) {
        this.orderGateway = requireNonNull(orderGateway, "'orderGateway' should not be null");
        this.spotGateway = requireNonNull(spotGateway, "'spotGateway' should not be null");
        this.clock = requireNonNull(clock, "'clock' should not be null");
    }

    @Override
    public Either<Notification, ExpireOrdersOutput> execute() {
        try {
            final var now = clock.instant();
            final var expiredIds = new ArrayList<String>();
            for (final Order order : orderGateway.findPendingExpired(now)) {
                if (!order.expireIfElapsed(now)) {
                    continue;
                }
                for (final var item : order.getItems()) {
                    spotGateway.findById(item.getSpotId()).ifPresent(spot -> {
                        spot.release();
                        spotGateway.update(spot);
                    });
                }
                orderGateway.update(order);
                expiredIds.add(order.getId().getValue());
            }
            return Either.right(ExpireOrdersOutput.from(expiredIds));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
