package com.tickethub.domain.core.spot;

import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.ValidationHandler;
import com.tickethub.domain.validation.Validator;

public final class SpotValidator extends Validator {
    private final Spot spot;
    public SpotValidator(final Spot spot, final ValidationHandler handler) { super(handler); this.spot = spot; }
    @Override public void validate() {
        if (spot.getLocation() == null) validationHandler().append(new Error("'location' should not be null"));
    }
}
