package com.tickethub.infrastructure.order.persistence;

import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.order.OrderItem;
import com.tickethub.domain.core.order.OrderStatus;
import com.tickethub.domain.core.payment.ChargeID;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.infrastructure.audit.AuditActor;
import com.tickethub.infrastructure.shared.persistence.MoneyDocument;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("orders")
public record OrderDocument(
        @Id String id,
        String customerId,
        List<OrderItemDocument> items,
        MoneyDocument total,
        String status,
        Instant expiresAt,
        String chargeId,
        String idempotencyKey,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        String createdBy,
        String lastModifiedBy) {

    public static final String COLLECTION = "orders";

    public record OrderItemDocument(String spotId, MoneyDocument price) {

        public static OrderItemDocument from(final OrderItem item) {
            return new OrderItemDocument(item.getSpotId().getValue(), MoneyDocument.from(item.getPrice()));
        }

        public OrderItem toDomain() {
            return OrderItem.of(SpotID.from(spotId), price.toDomain());
        }
    }

    public OrderDocument withActors(final String createdBy, final String lastModifiedBy) {
        return new OrderDocument(
                id,
                customerId,
                items,
                total,
                status,
                expiresAt,
                chargeId,
                idempotencyKey,
                createdAt,
                updatedAt,
                deletedAt,
                createdBy,
                lastModifiedBy);
    }

    public static OrderDocument from(final Order order) {
        return stamped(new OrderDocument(
                order.getId().getValue(),
                order.getCustomerId().getValue(),
                order.getItems().stream().map(OrderItemDocument::from).toList(),
                MoneyDocument.from(order.getTotal()),
                order.getStatus().name(),
                order.getExpiresAt(),
                Optional.ofNullable(order.getChargeId()).map(ChargeID::getValue).orElse(null),
                order.getIdempotencyKey(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getDeletedAt(),
                order.getCreatedBy(),
                order.getLastModifiedBy()));
    }

    private static OrderDocument stamped(final OrderDocument document) {
        final var actor = AuditActor.currentOrAnonymous();
        final var createdBy = Optional.ofNullable(document.createdBy()).orElse(actor);
        return document.withActors(createdBy, actor);
    }

    public Order toDomain() {
        return Order.reconstitute(
                OrderID.from(id),
                CustomerID.from(customerId),
                items.stream().map(OrderItemDocument::toDomain).toList(),
                Optional.ofNullable(total).map(MoneyDocument::toDomain).orElse(null),
                OrderStatus.valueOf(status),
                expiresAt,
                Optional.ofNullable(chargeId).map(ChargeID::from).orElse(null),
                idempotencyKey,
                createdAt,
                updatedAt,
                deletedAt,
                createdBy,
                lastModifiedBy);
    }
}
