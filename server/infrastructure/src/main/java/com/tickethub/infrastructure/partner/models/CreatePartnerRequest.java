package com.tickethub.infrastructure.partner.models;

import com.tickethub.infrastructure.api.models.*;

public record CreatePartnerRequest(String name, String cnpj, AddressModel address, String email, String password) {}
