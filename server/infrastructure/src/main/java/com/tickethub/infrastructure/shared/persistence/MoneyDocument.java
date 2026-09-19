package com.tickethub.infrastructure.shared.persistence;

import java.math.BigDecimal;
import java.util.Currency;

import com.tickethub.domain.shared.Money;

public record MoneyDocument(BigDecimal value, String currency) {

    public static MoneyDocument from(final Money money) {
        if (money == null) {
            return null;
        }
        return new MoneyDocument(money.getValue(), money.getCurrency().getCurrencyCode());
    }

    public Money toDomain() {
        return Money.create(value, Currency.getInstance(currency));
    }
}
