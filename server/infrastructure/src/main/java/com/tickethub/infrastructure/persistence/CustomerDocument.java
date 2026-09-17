package com.tickethub.infrastructure.persistence;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.shared.CPF;
import com.tickethub.domain.shared.Name;

@Document("customers")
public record CustomerDocument(
        @Id String id,
        String cpf,
        String name,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        String createdBy,
        String lastModifiedBy) {

    public static final String COLLECTION = "customers";

    public static CustomerDocument from(final Customer customer) {
        return new CustomerDocument(
                customer.getId().getValue(),
                customer.getCpf().getValue(),
                customer.getName().getValue(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getDeletedAt(),
                customer.getCreatedBy(),
                customer.getLastModifiedBy());
    }

    public Customer toDomain() {
        return Customer.reconstitute(
                CustomerID.from(id),
                CPF.create(cpf),
                Name.create(name),
                createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }
}
