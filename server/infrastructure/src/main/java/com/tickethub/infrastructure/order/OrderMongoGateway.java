package com.tickethub.infrastructure.order;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.order.OrderStatus;
import com.tickethub.domain.core.payment.ChargeID;
import com.tickethub.infrastructure.order.persistence.OrderDocument;
import com.tickethub.infrastructure.order.persistence.OrderRepository;

@Component
public class OrderMongoGateway implements OrderGateway {

    private final MongoTemplate mongoTemplate;
    private final OrderRepository repository;

    public OrderMongoGateway(final MongoTemplate mongoTemplate, final OrderRepository repository) {
        this.mongoTemplate = requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
        this.repository = requireNonNull(repository, "'repository' should not be null");
    }

    @Override
    public Order create(final Order order) {
        return mongoTemplate.insert(OrderDocument.from(order), OrderDocument.COLLECTION).toDomain();
    }

    @Override
    public Optional<Order> findById(final OrderID id) {
        requireNonNull(id, "'id' should not be null");
        return repository.findById(id.getValue()).map(OrderDocument::toDomain);
    }

    @Override
    public Optional<Order> findByChargeId(final ChargeID chargeId) {
        requireNonNull(chargeId, "'chargeId' should not be null");
        return repository.findByChargeId(chargeId.getValue()).map(OrderDocument::toDomain);
    }

    @Override
    public Optional<Order> findByIdempotencyKey(final String idempotencyKey) {
        requireNonNull(idempotencyKey, "'idempotencyKey' should not be null");
        return repository.findByIdempotencyKey(idempotencyKey).map(OrderDocument::toDomain);
    }

    @Override
    public List<Order> findPendingExpired(final Instant now) {
        requireNonNull(now, "'now' should not be null");
        return repository.findByStatusAndExpiresAtBefore(OrderStatus.PENDING.name(), now).stream()
                .map(OrderDocument::toDomain)
                .toList();
    }

    @Override
    public Order update(final Order order) {
        return mongoTemplate.save(OrderDocument.from(order), OrderDocument.COLLECTION).toDomain();
    }
}
