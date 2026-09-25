package com.tickethub.application.partner.reject;

import com.tickethub.domain.core.partner.Partner;

public record RejectPartnerOutput(String id, String status) {
    public static RejectPartnerOutput from(final Partner entity) {
        return new RejectPartnerOutput(
                entity.getId().getValue(), entity.getStatus().name());
    }
}
