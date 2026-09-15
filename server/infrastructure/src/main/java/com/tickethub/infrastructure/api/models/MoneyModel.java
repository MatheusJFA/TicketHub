package com.tickethub.infrastructure.api.models;

import java.math.BigDecimal;
import java.util.Currency;
import com.tickethub.domain.shared.Money;
import com.tickethub.domain.exception.DomainException;

public record MoneyModel(BigDecimal value, String currency) {
    public Money toDomain() {
        try {
            return Money.create(value, currency == null ? null : Currency.getInstance(currency));
        } catch (IllegalArgumentException exception) {
            throw new DomainException("Invalid currency");
        }
    }

    public static MoneyModel from(Money value) {
        return value == null ? null : new MoneyModel(value.getValue(), value.getCurrency().getCurrencyCode());
    }
}
