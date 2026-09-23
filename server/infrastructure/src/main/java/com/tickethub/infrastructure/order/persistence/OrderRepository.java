package com.tickethub.infrastructure.order.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<OrderDocument, String> {

    Optional<OrderDocument> findByChargeId(String chargeId);

    Optional<OrderDocument> findByIdempotencyKey(String idempotencyKey);

    List<OrderDocument> findByStatusAndExpiresAtBefore(String status, Instant now);
}
