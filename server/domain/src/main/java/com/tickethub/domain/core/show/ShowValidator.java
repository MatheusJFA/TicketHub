package com.tickethub.domain.core.show;

import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.ValidationHandler;
import com.tickethub.domain.validation.Validator;

public final class ShowValidator extends Validator {
    private final Show show;

    public ShowValidator(final Show show, final ValidationHandler handler) {
        super(handler);
        this.show = show;
    }

    @Override
    public void validate() {
        if (show.getName() == null)
            validationHandler().append(new Error("'name' should not be null"));
        if (show.getPartnerId() == null)
            validationHandler().append(new Error("'partnerId' should not be null"));
        if (show.getTotalSpots() < 0)
            validationHandler().append(new Error("'totalSpots' should not be negative"));
        if (show.getTotalSpotsSold() < 0)
            validationHandler().append(new Error("'totalSpotsSold' should not be negative"));
    }
}
