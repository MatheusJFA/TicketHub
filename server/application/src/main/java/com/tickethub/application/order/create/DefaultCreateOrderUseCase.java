package com.tickethub.application.order.create;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.tickethub.application.Either;
import com.tickethub.domain.core.coupon.Coupon;
import com.tickethub.domain.core.coupon.CouponGateway;
import com.tickethub.domain.core.coupon.CouponKind;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderItem;
import com.tickethub.domain.core.section.Section;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final CouponGateway couponGateway;
    private final Duration reservationTtl;
    private final Clock clock;

    public DefaultCreateOrderUseCase(
            final CustomerGateway customerGateway,
            final SpotGateway spotGateway,
            final SectionGateway sectionGateway,
            final OrderGateway orderGateway,
            final Duration reservationTtl) {
        this(customerGateway, spotGateway, sectionGateway, orderGateway, null, reservationTtl, Clock.systemUTC());
    }

    public DefaultCreateOrderUseCase(
            final CustomerGateway customerGateway,
            final SpotGateway spotGateway,
            final SectionGateway sectionGateway,
            final OrderGateway orderGateway,
            final Duration reservationTtl,
            final Clock clock) {
        this(customerGateway, spotGateway, sectionGateway, orderGateway, null, reservationTtl, clock);
    }

    public DefaultCreateOrderUseCase(
            final CustomerGateway customerGateway,
            final SpotGateway spotGateway,
            final SectionGateway sectionGateway,
            final OrderGateway orderGateway,
            final CouponGateway couponGateway,
            final Duration reservationTtl,
            final Clock clock) {
        this.customerGateway = requireNonNull(customerGateway, "'customerGateway' should not be null");
        this.spotGateway = requireNonNull(spotGateway, "'spotGateway' should not be null");
        this.sectionGateway = requireNonNull(sectionGateway, "'sectionGateway' should not be null");
        this.orderGateway = requireNonNull(orderGateway, "'orderGateway' should not be null");
        this.couponGateway = couponGateway;
        this.reservationTtl = requireNonNull(reservationTtl, "'reservationTtl' should not be null");
        this.clock = requireNonNull(clock, "'clock' should not be null");
    }

    @Override
    public Either<Notification, CreateOrderOutput> execute(final CreateOrderCommand command) {
        try {
            final var replayed = findReplay(command.idempotencyKey());
            if (replayed.isPresent()) {
                return Either.right(CreateOrderOutput.from(replayed.get()));
            }
            final var customerId = CustomerID.from(command.customerId());
            if (customerGateway.findById(customerId).isEmpty()) {
                return Either.left(notFound("Customer", customerId.getValue()));
            }
            if (isNull(command.spotIds()) || command.spotIds().isEmpty()) {
                return Either.left(Notification.create(new DomainException("'spotIds' should not be empty")));
            }

            final var reserved = new ArrayList<Spot>();
            final var items = new ArrayList<OrderItem>();
            final var placements = new ArrayList<SpotPlacement>();
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
                if (price.isEmpty()) {
                    releaseAll(reserved);
                    return Either.left(
                            Notification.create(new DomainException("Spot is not on sale: " + spotId.getValue())));
                }
                items.add(OrderItem.of(spotId, price.get()));
                placements.add(found);
            }

            final var discounted = applyCoupon(command.couponCode(), items, placements);
            if (discounted.isLeft()) {
                releaseAll(reserved);
                return Either.left(discounted.getLeft());
            }

            final var order =
                    Order.create(customerId, discounted.getRight(), reservationTtl, clock, command.idempotencyKey());
            final Notification notification = Notification.create();
            order.validate(notification);
            if (notification.hasError()) {
                releaseAll(reserved);
                return Either.left(notification);
            }
            if (isNotBlank(command.couponCode())) {
                final var claimed = claimCoupon(command.couponCode());
                if (claimed.isEmpty()) {
                    releaseAll(reserved);
                    return Either.left(Notification.create(
                            new DomainException("Coupon is exhausted or expired: " + command.couponCode())));
                }
            }
            return Either.right(CreateOrderOutput.from(persist(order)));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }

    private Optional<Order> findReplay(final String idempotencyKey) {
        if (isBlank(idempotencyKey)) {
            return Optional.empty();
        }
        return orderGateway.findByIdempotencyKey(idempotencyKey);
    }

    /**
     * Persists the reserved order. On concurrent retries with the same key the
     * unique index rejects the second insert: re-read the winner and return it
     * instead of failing. Framework-free on purpose (no Spring imports in the
     * application layer): any persistence failure with a key falls back to a
     * lookup, and only surfaces the error when nothing was stored.
     */
    private Order persist(final Order order) {
        if (isBlank(order.getIdempotencyKey())) {
            return orderGateway.create(order);
        }
        try {
            return orderGateway.create(order);
        } catch (final RuntimeException conflict) {
            return findReplay(order.getIdempotencyKey()).orElseThrow(() -> conflict);
        }
    }

    private Optional<Money> sectionPrice(final String sectionId) {
        return Optional.ofNullable(sectionId)
                .flatMap(id -> sectionGateway.findById(SectionID.from(id)))
                .map(Section::getPrice);
    }

    private Either<Notification, List<OrderItem>> applyCoupon(
            final String couponCode, final List<OrderItem> items, final List<SpotPlacement> placements) {
        if (!isNotBlank(couponCode)) {
            return Either.right(items);
        }
        if (couponGateway == null) {
            return Either.left(Notification.create(new DomainException("Coupons are not configured")));
        }
        final var coupon = couponGateway
                .findByCode(Coupon.normalizeCode(couponCode))
                .filter(found -> found.isActiveAt(clock.instant()));
        if (coupon.isEmpty()) {
            return Either.left(
                    Notification.create(new DomainException("Coupon is unknown, expired or exhausted: " + couponCode)));
        }
        final var current = coupon.get();
        final var eligible = new ArrayList<Integer>();
        for (int index = 0; index < items.size(); index++) {
            final var placement = placements.get(index);
            final var price = items.get(index).getPrice();
            if (!current.appliesTo(placement.showId(), placement.sectionId())) {
                continue;
            }
            if (current.getKind() == CouponKind.FIXED
                    && !current.getFixed().getCurrency().equals(price.getCurrency())) {
                return Either.left(Notification.create(
                        new DomainException("Coupon currency does not match order currency: " + couponCode)));
            }
            eligible.add(index);
        }
        if (eligible.isEmpty()) {
            return Either.left(
                    Notification.create(new DomainException("Coupon does not apply to these spots: " + couponCode)));
        }
        return Either.right(discountedItems(current, items, eligible));
    }

    private static List<OrderItem> discountedItems(
            final Coupon coupon, final List<OrderItem> items, final List<Integer> eligible) {
        if (coupon.getKind() == CouponKind.PERCENT) {
            final var factor = BigDecimal.valueOf(100 - coupon.getPercent())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            final var discounted = new ArrayList<>(items);
            for (final int index : eligible) {
                final var item = items.get(index);
                discounted.set(
                        index, OrderItem.of(item.getSpotId(), item.getPrice().multiply(factor)));
            }
            return discounted;
        }
        Money subtotal = null;
        for (final int index : eligible) {
            final var price = items.get(index).getPrice();
            subtotal = subtotal == null ? price : subtotal.add(price);
        }
        final var fixed = coupon.getFixed();
        final var currency = subtotal.getCurrency();
        final var zero = Money.create(BigDecimal.ZERO, currency);
        final var discount = fixed.getValue().compareTo(subtotal.getValue()) >= 0 ? subtotal : fixed;
        final var discounted = new ArrayList<>(items);
        Money distributed = zero;
        for (int cursor = 0; cursor < eligible.size(); cursor++) {
            final int index = eligible.get(cursor);
            final var price = items.get(index).getPrice();
            final Money share;
            if (cursor == eligible.size() - 1) {
                share = discount.subtract(distributed);
            } else {
                share = Money.create(
                        discount.getValue()
                                .multiply(price.getValue())
                                .divide(subtotal.getValue(), currency.getDefaultFractionDigits(), RoundingMode.HALF_UP),
                        currency);
                distributed = distributed.add(share);
            }
            discounted.set(index, OrderItem.of(items.get(index).getSpotId(), price.subtract(share)));
        }
        return discounted;
    }

    private Optional<Coupon> claimCoupon(final String couponCode) {
        if (couponGateway == null) {
            return Optional.empty();
        }
        return couponGateway.claimUse(Coupon.normalizeCode(couponCode), clock.instant());
    }

    private void releaseAll(final List<Spot> reserved) {
        reserved.forEach(spot -> {
            try {
                spot.release();
                spotGateway.update(spot);
            } catch (final RuntimeException ignored) {
                // Best effort: the sweeper frees spots of expired orders, and a
                // failed release here must not mask the original failure.
            }
        });
    }
}
