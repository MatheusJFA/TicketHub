package com.tickethub.infrastructure.partner.models;

import com.tickethub.infrastructure.api.models.*;

public record UpdatePartnerRequest(String name, AddressModel address) {}
