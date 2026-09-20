package com.tickethub.infrastructure.shared.persistence;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import com.tickethub.domain.shared.Money;

public record MoneyDocument(BigDecimal value, String currency) {

    public static MoneyDocument from(final Money money) {
        return Optional.ofNullable(money)
                .map(current -> new MoneyDocument(current.getValue(),
                        current.getCurrency().getCurrencyCode()))
                .orElse(null);
    }

    public Money toDomain() {
        return Money.create(value, Currency.getInstance(currency));
    }
}
