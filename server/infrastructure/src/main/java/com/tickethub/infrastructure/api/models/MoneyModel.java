package com.tickethub.infrastructure.api.models;

import java.math.BigDecimal;

public record MoneyModel(BigDecimal value, String currency) {
}
