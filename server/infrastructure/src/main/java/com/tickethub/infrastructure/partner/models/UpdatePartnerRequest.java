package com.tickethub.infrastructure.partner.models;

import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;

public record UpdatePartnerRequest(String name, AddressModel address) {}
