package com.tickethub.domain.core.order;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.ValueObject;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.shared.Money;
import java.util.Objects;

/**
 * A reserved spot with its price snapshot. The price is frozen at purchase
 * time so later section price changes do not affect open orders.
 */
public class OrderItem extends ValueObject {
    private final SpotID spotId;
    private final Money price;

    private OrderItem(final SpotID spotId, final Money price) {
        this.spotId = requireNonNull(spotId, "'spotId' should not be null");
        this.price = requireNonNull(price, "'price' should not be null");
    }

    public static OrderItem of(final SpotID spotId, final Money price) {
        return new OrderItem(spotId, price);
    }

    public SpotID getSpotId() {
        return spotId;
    }

    public Money getPrice() {
        return price;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof OrderItem item
                && spotId.equals(item.spotId)
                && price.equals(item.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spotId, price);
    }
}
