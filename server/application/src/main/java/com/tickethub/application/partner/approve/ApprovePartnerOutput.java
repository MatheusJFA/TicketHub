package com.tickethub.application.partner.approve;

import com.tickethub.domain.core.partner.Partner;

public record ApprovePartnerOutput(String id, String status) {
    public static ApprovePartnerOutput from(final Partner entity) {
        return new ApprovePartnerOutput(
                entity.getId().getValue(), entity.getStatus().name());
    }
}
