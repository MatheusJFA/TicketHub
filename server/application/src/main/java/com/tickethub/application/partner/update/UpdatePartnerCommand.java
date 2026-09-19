package com.tickethub.application.partner.update;

import com.tickethub.domain.shared.Address;

public record UpdatePartnerCommand(String id, String name, Address address) {
    public static UpdatePartnerCommand with(final String id, final String name, final Address address) {
        return new UpdatePartnerCommand(id, name, address);
    }
}
