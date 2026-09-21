package com.tickethub.infrastructure.ticket.persistence;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.core.ticket.Ticket;
import com.tickethub.domain.core.ticket.TicketID;
import com.tickethub.domain.core.ticket.TicketStatus;
import com.tickethub.infrastructure.audit.AuditActor;

@Document("tickets")
public record TicketDocument(
        @Id String id,
        String orderId,
        String spotId,
        String customerId,
        String code,
        String signature,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        String createdBy,
        String lastModifiedBy) {

    public static final String COLLECTION = "tickets";

    public TicketDocument withActors(final String createdBy, final String lastModifiedBy) {
        return new TicketDocument(id, orderId, spotId, customerId, code, signature, status,
                createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }

    public static TicketDocument from(final Ticket ticket) {
        return stamped(new TicketDocument(
                ticket.getId().getValue(),
                ticket.getOrderId().getValue(),
                ticket.getSpotId().getValue(),
                ticket.getCustomerId().getValue(),
                ticket.getCode(),
                ticket.getSignature(),
                ticket.getStatus().name(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getDeletedAt(),
                ticket.getCreatedBy(),
                ticket.getLastModifiedBy()));
    }

    private static TicketDocument stamped(final TicketDocument document) {
        final var actor = AuditActor.currentOrAnonymous();
        final var createdBy = Optional.ofNullable(document.createdBy()).orElse(actor);
        return document.withActors(createdBy, actor);
    }

    public Ticket toDomain() {
        return Ticket.reconstitute(
                TicketID.from(id),
                OrderID.from(orderId),
                SpotID.from(spotId),
                CustomerID.from(customerId),
                code,
                signature,
                TicketStatus.valueOf(status),
                createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }
}
