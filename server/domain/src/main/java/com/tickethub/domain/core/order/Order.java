package com.tickethub.domain.core.order;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import com.tickethub.domain.AggregateRoot;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.exception.IllegalOrderTransitionException;
import com.tickethub.domain.exception.OrderExpiredException;
import com.tickethub.domain.shared.Money;
import com.tickethub.domain.validation.ValidationHandler;

public class Order extends AggregateRoot<OrderID> {
    private final CustomerID customerId;
    private final List<OrderItem> items;
    private final Money total;
    private OrderStatus status;
    private final Instant expiresAt;
    private String chargeId;

    private Order(OrderID id, CustomerID customerId, List<OrderItem> items, Money total,
            OrderStatus status, Instant expiresAt, String chargeId,
            Instant createdAt, Instant updatedAt, Instant deletedAt, String createdBy, String lastModifiedBy) {
        super(id, createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
        this.customerId = customerId;
        this.items = items;
        this.total = total;
        this.status = status;
        this.expiresAt = expiresAt;
        this.chargeId = chargeId;
    }

    /**
     * Opens a PENDING order and reserves its spots for {@code reservationTtl}.
     * Item prices must share a single currency; the total is their sum.
     */
    public static Order create(final CustomerID customerId, final List<OrderItem> items,
            final Duration reservationTtl) {
        return create(customerId, items, reservationTtl, Clock.systemUTC());
    }

    public static Order create(final CustomerID customerId, final List<OrderItem> items,
            final Duration reservationTtl, final Clock clock) {
        requireNonNull(customerId, "'customerId' should not be null");
        requireNonNull(items, "'items' should not be null");
        requireNonNull(reservationTtl, "'reservationTtl' should not be null");
        requireNonNull(clock, "'clock' should not be null");
        if (items.isEmpty()) {
            throw new DomainException("'items' should not be empty");
        }
        if (reservationTtl.isZero() || reservationTtl.isNegative()) {
            throw new DomainException("'reservationTtl' should be positive");
        }
        final var total = sum(items);
        final var now = clock.instant();
        final var order = new Order(OrderID.generate(), customerId, List.copyOf(items), total,
                OrderStatus.PENDING, now.plus(reservationTtl), null, now, now, null, null, null);
        order.registerEvent(new OrderCreated(order.getId().getValue(), customerId.getValue(),
                total, order.expiresAt, now));
        return order;
    }

    public static Order reconstitute(OrderID id, CustomerID customerId, List<OrderItem> items, Money total,
            OrderStatus status, Instant expiresAt, String chargeId,
            Instant createdAt, Instant updatedAt, Instant deletedAt, String createdBy, String lastModifiedBy) {
        return new Order(id, customerId, items, total, status, expiresAt, chargeId,
                createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }

    private static Money sum(final List<OrderItem> items) {
        final Currency currency = items.get(0).getPrice().getCurrency();
        Money total = Money.create(java.math.BigDecimal.ZERO, currency);
        for (final OrderItem item : items) {
            total = total.add(item.getPrice());
        }
        return total;
    }

    /**
     * Links the external payment charge created for this order. Only open
     * (PENDING) orders can be charged; the actual payment confirmation
     * arrives later through {@link #markAsPaid()}.
     */
    public void attachCharge(final String chargeId) {
        requireNonNull(chargeId, "'chargeId' should not be null");
        requirePendingFor(OrderStatus.PENDING);
        this.chargeId = chargeId;
        markAsUpdated();
    }

    /**
     * Confirms payment (usually from the provider webhook) and releases the
     * order for ticket issuance. Uses the given clock so expiry checks stay
     * deterministic in tests.
     */
    public void markAsPaid(final Clock clock) {
        requireNonNull(clock, "'clock' should not be null");
        if (isExpired(clock.instant())) {
            expire();
            throw new OrderExpiredException();
        }
        requirePendingFor(OrderStatus.PAID);
        this.status = OrderStatus.PAID;
        markAsUpdated();
        registerEvent(new OrderPaid(getId().getValue(), clock.instant()));
    }

    public void markAsPaid() {
        markAsPaid(Clock.systemUTC());
    }

    /**
     * Expires an open order and releases its spots. Terminal orders are left
     * untouched (idempotent, like {@link #delete()}), except PAID which can
     * never expire.
     */
    public void expire() {
        if (status == OrderStatus.PAID) {
            throw new IllegalOrderTransitionException(status, OrderStatus.EXPIRED);
        }
        if (status != OrderStatus.PENDING) {
            return;
        }
        this.status = OrderStatus.EXPIRED;
        markAsUpdated();
        registerEvent(new OrderExpired(getId().getValue(), Instant.now()));
    }

    /**
     * Cancels an open order at the buyer's request and releases its spots.
     */
    public void cancel() {
        if (status == OrderStatus.PAID) {
            throw new IllegalOrderTransitionException(status, OrderStatus.CANCELLED);
        }
        if (status != OrderStatus.PENDING) {
            return;
        }
        this.status = OrderStatus.CANCELLED;
        markAsUpdated();
        registerEvent(new OrderCancelled(getId().getValue(), Instant.now()));
    }

    /**
     * Moves a PENDING order to EXPIRED when its reservation TTL elapsed.
     * Returns true when the order expired here; used by the expiry sweeper.
     */
    public boolean expireIfElapsed(final Instant now) {
        requireNonNull(now, "'now' should not be null");
        if (!isExpired(now)) {
            return false;
        }
        expire();
        return true;
    }

    public boolean isExpired(final Instant now) {
        requireNonNull(now, "'now' should not be null");
        return status == OrderStatus.PENDING && !now.isBefore(expiresAt);
    }

    private void requirePendingFor(final OrderStatus target) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalOrderTransitionException(status, target);
        }
    }

    @Override
    public void validate(final ValidationHandler handler) {
        new OrderValidator(this, handler).validate();
    }

    public CustomerID getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public Money getTotal() {
        return total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getChargeId() {
        return chargeId;
    }

    public boolean hasCharge() {
        return !isNull(chargeId);
    }
}
