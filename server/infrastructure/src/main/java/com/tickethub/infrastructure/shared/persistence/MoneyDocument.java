package com.tickethub.infrastructure.shared.persistence;

import com.tickethub.domain.shared.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

public record MoneyDocument(BigDecimal value, String currency) {

    public static MoneyDocument from(final Money money) {
        return Optional.ofNullable(money).map(MoneyDocument::with).orElse(null);
    }

    static MoneyDocument with(final Money current) {
        return new MoneyDocument(current.getValue(), current.getCurrency().getCurrencyCode());
    }

    public Money toDomain() {
        return Money.create(value, Currency.getInstance(currency));
    }
}
