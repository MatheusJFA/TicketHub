package com.tickethub.application.partner.changeaddress;
import com.tickethub.domain.core.partner.Partner;
public record ChangePartnerAddressOutput(String id) {
    public static ChangePartnerAddressOutput from(final Partner entity) {
        return new ChangePartnerAddressOutput(entity.getId().getValue());
    }
}
