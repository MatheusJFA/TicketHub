package com.tickethub.infrastructure.customer.models;

import java.time.Instant;
import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;
import com.tickethub.application.customer.retrieve.list.ListCustomersOutput;

public record CustomerListResponse(String id, String name, String cpf, Instant createdAt, Instant updatedAt, Instant deletedAt) {
    public static CustomerListResponse from(ListCustomersOutput output) {
        return new CustomerListResponse(output.id(), output.name(), output.cpf(), output.createdAt(), output.updatedAt(), output.deletedAt());
    }
}
