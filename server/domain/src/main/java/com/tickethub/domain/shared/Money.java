package com.tickethub.domain.shared;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.tickethub.domain.ValueObject;
import com.tickethub.domain.exception.DomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public final class Money extends ValueObject {
    private final BigDecimal value;
    private final Currency currency;

    private Money(BigDecimal value, Currency currency) {
        this.value = value;
        this.currency = currency;
    }

    public static Money create(BigDecimal value, Currency currency) {
        if (!isValid(value, currency)) {
            throw new DomainException("Invalid money");
        }

        try {
            final BigDecimal normalizedValue =
                    value.setScale(currency.getDefaultFractionDigits(), RoundingMode.UNNECESSARY);
            return new Money(normalizedValue, currency);
        } catch (final ArithmeticException e) {
            throw new DomainException("Invalid money", e);
        }
    }

    private static boolean isValid(BigDecimal value, Currency currency) {
        return nonNull(value)
                && nonNull(currency)
                && currency.getDefaultFractionDigits() >= 0
                && value.signum() >= 0
                && value.stripTrailingZeros().scale() <= currency.getDefaultFractionDigits();
    }

    /**
     * Sums two amounts in the same currency, used to total order items whose
     * prices were snapshotted at purchase time.
     */
    public Money add(final Money other) {
        if (isNull(other)) {
            throw new DomainException("'other' should not be null");
        }
        if (!currency.equals(other.currency)) {
            throw new DomainException("Cannot add money with different currencies");
        }
        return Money.create(value.add(other.value), currency);
    }

    public Money subtract(final Money other) {
        if (isNull(other)) {
            throw new DomainException("'other' should not be null");
        }
        if (!currency.equals(other.currency)) {
            throw new DomainException("Cannot subtract money with different currencies");
        }
        return Money.create(value.subtract(other.value), currency);
    }

    public Money multiply(final BigDecimal factor) {
        if (isNull(factor) || factor.signum() < 0) {
            throw new DomainException("'factor' should not be null or negative");
        }
        return Money.create(
                value.multiply(factor).setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP), currency);
    }

    public BigDecimal getValue() {
        return value;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Money money && value.equals(money.value) && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, currency);
    }

    @Override
    public String toString() {
        return currency.getCurrencyCode() + " " + value.toPlainString();
    }
}
