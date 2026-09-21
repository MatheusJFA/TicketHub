package com.tickethub.application.order.create;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.tickethub.application.Either;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderItem;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.core.spot.SpotPlacement;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.exception.SpotUnavailableException;
import com.tickethub.domain.shared.Money;
import com.tickethub.domain.validation.Notification;

/**
 * Opens a PENDING order and atomically reserves each spot. Spots must be
 * published and free; the price is snapshotted from their sections. When any
 * item fails, already reserved spots are released so no seat leaks.
 */
public class DefaultCreateOrderUseCase extends CreateOrderUseCase {
    private final CustomerGateway customerGateway;
    private final SpotGateway spotGateway;
    private final SectionGateway sectionGateway;
    private final OrderGateway orderGateway;
    private final Duration reservationTtl;
    private final Clock clock;

    public DefaultCreateOrderUseCase(final CustomerGateway customerGateway, final SpotGateway spotGateway,
            final SectionGateway sectionGateway, final OrderGateway orderGateway,
            final Duration reservationTtl) {
        this(customerGateway, spotGateway, sectionGateway, orderGateway, reservationTtl, Clock.systemUTC());
    }

    public DefaultCreateOrderUseCase(final CustomerGateway customerGateway, final SpotGateway spotGateway,
            final SectionGateway sectionGateway, final OrderGateway orderGateway,
            final Duration reservationTtl, final Clock clock) {
        this.customerGateway = requireNonNull(customerGateway, "'customerGateway' should not be null");
        this.spotGateway = requireNonNull(spotGateway, "'spotGateway' should not be null");
        this.sectionGateway = requireNonNull(sectionGateway, "'sectionGateway' should not be null");
        this.orderGateway = requireNonNull(orderGateway, "'orderGateway' should not be null");
        this.reservationTtl = requireNonNull(reservationTtl, "'reservationTtl' should not be null");
        this.clock = requireNonNull(clock, "'clock' should not be null");
    }

    @Override
    public Either<Notification, CreateOrderOutput> execute(final CreateOrderCommand command) {
        try {
            final var customerId = CustomerID.from(command.customerId());
            if (customerGateway.findById(customerId).isEmpty()) {
                return Either.left(notFound("Customer", customerId.getValue()));
            }
            if (command.spotIds() == null || command.spotIds().isEmpty()) {
                return Either.left(
                        Notification.create(new DomainException("'spotIds' should not be empty")));
            }

            final var reserved = new ArrayList<Spot>();
            final var items = new ArrayList<OrderItem>();
            for (final String rawSpotId : command.spotIds()) {
                final var spotId = SpotID.from(rawSpotId);
                final var placement = spotGateway.findPlacement(spotId);
                if (placement.isEmpty()) {
                    releaseAll(reserved);
                    return Either.left(notFound("Spot", spotId.getValue()));
                }
                final SpotPlacement found = placement.get();
                if (!found.spot().isPublished()) {
                    releaseAll(reserved);
                    return Either.left(Notification.create(new SpotUnavailableException()));
                }
                final var taken = spotGateway.reserveIfAvailable(spotId);
                if (taken.isEmpty()) {
                    releaseAll(reserved);
                    return Either.left(Notification.create(new SpotUnavailableException()));
                }
                reserved.add(taken.get());
                final var price = sectionPrice(found.sectionId());
                if (price == null) {
                    releaseAll(reserved);
                    return Either.left(Notification.create(
                            new DomainException("Spot is not on sale: " + spotId.getValue())));
                }
                items.add(OrderItem.of(spotId, price));
            }

            final var order = Order.create(customerId, items, reservationTtl, clock);
            final Notification notification = Notification.create();
            order.validate(notification);
            if (notification.hasError()) {
                releaseAll(reserved);
                return Either.left(notification);
            }
            return Either.right(CreateOrderOutput.from(orderGateway.create(order)));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }

    private Money sectionPrice(final String sectionId) {
        if (sectionId == null) {
            return null;
        }
        return sectionGateway.findById(SectionID.from(sectionId))
                .map(section -> section.getPrice())
                .orElse(null);
    }

    private void releaseAll(final List<Spot> reserved) {
        for (final Spot spot : reserved) {
            try {
                spot.release();
                spotGateway.update(spot);
            } catch (final RuntimeException ignored) {
                // Best effort: the sweeper frees spots of expired orders, and a
                // failed release here must not mask the original failure.
            }
        }
    }
}
