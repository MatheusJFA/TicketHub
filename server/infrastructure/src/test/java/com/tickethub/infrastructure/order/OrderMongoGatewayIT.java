package com.tickethub.infrastructure.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.order.OrderItem;
import com.tickethub.domain.core.payment.ChargeID;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.shared.Money;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;
import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.order.persistence.OrderDocument;

@IntegrationTest
@DisplayName("Order Mongo gateway")
class OrderMongoGatewayIT extends ContainerSupport {

    private static final Currency BRL = Currency.getInstance("BRL");
    private static final Duration TTL = Duration.ofMinutes(15);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private OrderMongoGateway gateway;

    @BeforeEach
    void cleanUp() {
        MongoCleanUpExtension.cleanCollections(mongoTemplate, OrderDocument.COLLECTION);
    }

    private Order givenOrder() {
        return Order.create(
                CustomerID.generate(),
                List.of(OrderItem.of(SpotID.generate(), Money.create(new BigDecimal("50.00"), BRL))),
                TTL);
    }

    @Test
    @DisplayName("Given an order, when create, then persists and round trips")
    void givenAnOrder_whenCreate_thenPersistsAndRoundTrips() {
        final var order = givenOrder();

        gateway.create(order);

        final var found = gateway.findById(order.getId()).orElseThrow();
        assertEquals(order.getCustomerId(), found.getCustomerId());
        assertEquals(order.getTotal(), found.getTotal());
        assertEquals(order.getStatus(), found.getStatus());
        assertEquals(order.getExpiresAt().truncatedTo(java.time.temporal.ChronoUnit.MILLIS),
                found.getExpiresAt());
        assertEquals(1, found.getItems().size());
        assertEquals(order.getItems().get(0).getSpotId(), found.getItems().get(0).getSpotId());
        assertEquals(order.getItems().get(0).getPrice(), found.getItems().get(0).getPrice());
    }

    @Test
    @DisplayName("Given a paid order with charge, when find by charge id, then returns order")
    void givenPaidOrder_whenFindByChargeId_thenReturnsOrder() {
        final var order = givenOrder();
        order.attachCharge(ChargeID.from("ch_123"));
        order.markAsPaid();
        gateway.create(order);

        final var found = gateway.findByChargeId(ChargeID.from("ch_123")).orElseThrow();

        assertEquals(order.getId(), found.getId());
        assertEquals(ChargeID.from("ch_123"), found.getChargeId());
        assertFalse(gateway.findByChargeId(ChargeID.from("ch_missing")).isPresent());
        assertFalse(gateway.findById(OrderID.generate()).isPresent());
    }

    @Test
    @DisplayName("Given an order, when update, then persists status and charge")
    void givenAnOrder_whenUpdate_thenPersistsChanges() {
        final var order = gateway.create(givenOrder());

        order.attachCharge(ChargeID.from("ch_123"));
        order.markAsPaid();
        gateway.update(order);

        final var found = gateway.findById(order.getId()).orElseThrow();
        assertEquals(ChargeID.from("ch_123"), found.getChargeId());
        assertEquals(com.tickethub.domain.core.order.OrderStatus.PAID, found.getStatus());
    }

    @Test
    @DisplayName("Given expired and open orders, when find pending expired, then returns only expired")
    void givenOrders_whenFindPendingExpired_thenReturnsOnlyExpired() {
        final var past = Clock.fixed(Instant.now().minus(TTL).minusSeconds(60), ZoneOffset.UTC);
        final var expired = Order.create(CustomerID.generate(),
                List.of(OrderItem.of(SpotID.generate(), Money.create(BigDecimal.TEN, BRL))),
                TTL, past);
        final var open = givenOrder();
        gateway.create(expired);
        gateway.create(open);

        final var found = gateway.findPendingExpired(Instant.now());

        assertEquals(1, found.size());
        assertEquals(expired.getId(), found.get(0).getId());
    }
}
