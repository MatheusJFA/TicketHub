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
        final ValidationHandler handler = validationHandler();

        if (show.getName() == null)
            handler.append(new Error("'name' should not be null"));
        if (show.getPartnerId() == null)
            handler.append(new Error("'partnerId' should not be null"));
        if (show.getAddress() == null)
            handler.append(new Error("'address' should not be null"));
        if (show.getTotalSpots() < 0)
            handler.append(new Error("'totalSpots' should not be negative"));
        if (show.getTotalSpotsSold() < 0)
            handler.append(new Error("'totalSpotsSold' should not be negative"));
        show.getSections().forEach(section -> section.validate(handler));
    }
}
