package com.tickethub.infrastructure.customer.models;

import java.time.Instant;
import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;
import com.tickethub.application.customer.retrieve.get.GetCustomerOutput;

public record CustomerResponse(String id, String name, String cpf, Instant createdAt, Instant updatedAt, Instant deletedAt) {
    public static CustomerResponse from(GetCustomerOutput output) {
        return new CustomerResponse(output.id(), output.name(), output.cpf(), output.createdAt(), output.updatedAt(), output.deletedAt());
    }
}
