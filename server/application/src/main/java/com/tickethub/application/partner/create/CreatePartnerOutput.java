package com.tickethub.application.partner.create;

import com.tickethub.domain.core.partner.Partner;

public record CreatePartnerOutput(String id) {
    public static CreatePartnerOutput from(final String id) {
        return new CreatePartnerOutput(id);
    }

    public static CreatePartnerOutput from(final Partner entity) {
        return from(entity.getId().getValue());
    }
}
