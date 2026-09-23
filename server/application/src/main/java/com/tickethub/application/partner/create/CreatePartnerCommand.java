package com.tickethub.application.partner.create;

import com.tickethub.domain.shared.Address;

public record CreatePartnerCommand(String name, String cnpj, Address address, String email, String password) {
    public static CreatePartnerCommand with(
            final String name, final String cnpj, final Address address, final String email, final String password) {
        return new CreatePartnerCommand(name, cnpj, address, email, password);
    }
}
