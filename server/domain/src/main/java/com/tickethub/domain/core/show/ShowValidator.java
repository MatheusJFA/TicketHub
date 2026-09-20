package com.tickethub.domain.core.show;

import static java.util.Objects.isNull;

import com.tickethub.domain.validation.Validator;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.ValidationHandler;

public final class ShowValidator extends Validator {
    private final Show show;

    public ShowValidator(final Show show, final ValidationHandler handler) {
        super(handler);
        this.show = show;
    }

    @Override
    public void validate() {
        final ValidationHandler handler = validationHandler();

        if (isNull(show.getName()))
            handler.append(new Error("'name' should not be null"));
        if (isNull(show.getPartnerId()))
            handler.append(new Error("'partnerId' should not be null"));
        if (isNull(show.getAddress()))
            handler.append(new Error("'address' should not be null"));
        if (show.getTotalSpots() < 0)
            handler.append(new Error("'totalSpots' should not be negative"));
        if (show.getTotalSpotsSold() < 0)
            handler.append(new Error("'totalSpotsSold' should not be negative"));
        show.getSections().forEach(section -> section.validate(handler));
    }
}
