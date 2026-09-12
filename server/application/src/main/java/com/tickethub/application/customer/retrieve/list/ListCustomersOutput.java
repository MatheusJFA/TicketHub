package com.tickethub.application.customer.retrieve.list;

import java.time.Instant;

import com.tickethub.domain.core.customer.Customer;

public record ListCustomersOutput(
        String id,
        String name,
        String cpf,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt) {
    public static ListCustomersOutput from(final Customer entity) {
        return new ListCustomersOutput(
                entity.getId().getValue(),
                entity.getName().getValue(),
                entity.getCpf().getValue(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt());
    }
}
