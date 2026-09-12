package com.tickethub.application.partner.create;

import com.tickethub.domain.shared.Address;

public record CreatePartnerCommand(String name, String cnpj, Address address) {
    public static CreatePartnerCommand with(final String name, final String cnpj, final Address address) {
        return new CreatePartnerCommand(name, cnpj, address);
    }
}
