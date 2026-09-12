package com.tickethub.application.section.changeprice;

import com.tickethub.domain.shared.Money;

public record ChangeSectionPriceCommand(String id, Money price) {
    public static ChangeSectionPriceCommand with(final String id, final Money price) {
        return new ChangeSectionPriceCommand(id, price);
    }
}
