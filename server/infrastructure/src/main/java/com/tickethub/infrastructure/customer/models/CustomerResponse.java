package com.tickethub.infrastructure.customer.models;

import java.time.Instant;
import java.time.OffsetDateTime;

public record CustomerResponse(String id, String name, String cpf, Instant createdAt, Instant updatedAt, Instant deletedAt) {
}
