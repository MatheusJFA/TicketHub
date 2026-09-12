package com.tickethub.application.partner.changeaddress;

import com.tickethub.domain.shared.Address;

public record ChangePartnerAddressCommand(String id, Address address) {
    public static ChangePartnerAddressCommand with(final String id, final Address address) {
        return new ChangePartnerAddressCommand(id, address);
    }
}
