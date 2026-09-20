package com.tickethub.domain.core.section;

import static java.util.Objects.isNull;

import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.ValidationHandler;
import com.tickethub.domain.validation.Validator;

public final class SectionValidator extends Validator {
    private final Section section;

    public SectionValidator(final Section section, final ValidationHandler handler) {
        super(handler);
        this.section = section;
    }

    @Override
    public void validate() {
        final ValidationHandler handler = validationHandler();

        if (isNull(section.getName()))
            handler.append(new Error("'name' should not be null"));
        if (isNull(section.getPrice()))
            handler.append(new Error("'price' should not be null"));
        if (section.getTotalSpots() < 0)
            handler.append(new Error("'totalSpots' should not be negative"));
        if (section.getTotalSpotsSold() < 0)
            handler.append(new Error("'totalSpotsSold' should not be negative"));
    }
}
