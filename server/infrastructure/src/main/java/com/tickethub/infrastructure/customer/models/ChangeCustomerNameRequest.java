package com.tickethub.infrastructure.customer.models;

import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;

public record ChangeCustomerNameRequest(String name) {}
