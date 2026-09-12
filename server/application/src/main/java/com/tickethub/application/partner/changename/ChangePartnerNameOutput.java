package com.tickethub.application.partner.changename;
import com.tickethub.domain.core.partner.Partner;
public record ChangePartnerNameOutput(String id) {
    public static ChangePartnerNameOutput from(final Partner entity) {
        return new ChangePartnerNameOutput(entity.getId().getValue());
    }
}
