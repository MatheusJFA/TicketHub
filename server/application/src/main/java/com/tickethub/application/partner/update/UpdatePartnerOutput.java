package com.tickethub.application.partner.update;

import com.tickethub.domain.core.partner.Partner;

public record UpdatePartnerOutput(String id) {
    public static UpdatePartnerOutput from(final String id) {
        return new UpdatePartnerOutput(id);
    }

    public static UpdatePartnerOutput from(final Partner entity) {
        return from(entity.getId().getValue());
    }
}
