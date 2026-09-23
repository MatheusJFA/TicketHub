package com.tickethub.application.customer.retrieve.get;

import com.tickethub.domain.core.customer.Customer;
import java.time.Instant;

public record GetCustomerOutput(
        String id, String name, String cpf, Instant createdAt, Instant updatedAt, Instant deletedAt) {
    public static GetCustomerOutput from(final Customer entity) {
        return new GetCustomerOutput(
                entity.getId().getValue(),
                entity.getName().getValue(),
                entity.getCpf().getValue(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt());
    }
}
