package com.tickethub.domain.core.order;

import static java.util.Objects.isNull;

import java.math.BigDecimal;
import java.util.Currency;

import com.tickethub.domain.shared.Money;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.ValidationHandler;
import com.tickethub.domain.validation.Validator;

public final class OrderValidator extends Validator {
    private final Order order;

    public OrderValidator(final Order order, final ValidationHandler handler) {
        super(handler);
        this.order = order;
    }

    @Override
    public void validate() {
        final ValidationHandler handler = validationHandler();

        if (isNull(order.getCustomerId())) {
            handler.append(new Error("'customerId' should not be null"));
        }
        if (isNull(order.getItems())) {
            handler.append(new Error("'items' should not be null"));
        } else if (order.getItems().isEmpty()) {
            handler.append(new Error("'items' should not be empty"));
        }
        if (isNull(order.getTotal())) {
            handler.append(new Error("'total' should not be null"));
        } else if (!isNull(order.getItems()) && !order.getItems().isEmpty()) {
            final Currency currency = order.getItems().get(0).getPrice().getCurrency();
            Money expected = Money.create(BigDecimal.ZERO, currency);
            try {
                for (final OrderItem item : order.getItems()) {
                    expected = expected.add(item.getPrice());
                }
            } catch (final RuntimeException mixed) {
                handler.append(new Error("'items' should share a single currency"));
                return;
            }
            if (!expected.equals(order.getTotal())) {
                handler.append(new Error("'total' should be the sum of the item prices"));
            }
        }
        if (isNull(order.getStatus())) {
            handler.append(new Error("'status' should not be null"));
        }
        if (isNull(order.getExpiresAt())) {
            handler.append(new Error("'expiresAt' should not be null"));
        }
    }
}
