package com.tickethub.domain.core.order;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.tickethub.domain.core.payment.ChargeID;

public interface OrderGateway {
    Order create(Order order);
    Optional<Order> findById(OrderID id);
    Optional<Order> findByChargeId(ChargeID chargeId);
    List<Order> findPendingExpired(Instant now);
    Order update(Order order);
}
