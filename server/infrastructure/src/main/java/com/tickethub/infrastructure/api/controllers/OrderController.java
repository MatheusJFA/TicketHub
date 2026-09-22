package com.tickethub.infrastructure.api.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.tickethub.application.order.cancel.CancelOrderCommand;
import com.tickethub.application.order.cancel.CancelOrderUseCase;
import com.tickethub.application.order.create.CreateOrderCommand;
import com.tickethub.application.order.create.CreateOrderUseCase;
import com.tickethub.application.payment.pay.PayOrderCommand;
import com.tickethub.application.payment.pay.PayOrderUseCase;
import com.tickethub.application.order.retrieve.get.GetOrderUseCase;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.api.OrderAPI;
import com.tickethub.infrastructure.cache.TickethubCacheProperties;
import com.tickethub.infrastructure.order.models.CreateOrderRequest;
import com.tickethub.infrastructure.order.models.OrderResponse;
import com.tickethub.infrastructure.order.models.PayOrderResponse;
import org.springframework.cache.annotation.CacheEvict;

@RestController
public class OrderController implements OrderAPI {
    private final CreateOrderUseCase createOrder;
    private final PayOrderUseCase payOrder;
    private final CancelOrderUseCase cancelOrder;
    private final GetOrderUseCase getOrder;

    public OrderController(final CreateOrderUseCase createOrder, final PayOrderUseCase payOrder,
            final CancelOrderUseCase cancelOrder, final GetOrderUseCase getOrder) {
        this.createOrder = createOrder;
        this.payOrder = payOrder;
        this.cancelOrder = cancelOrder;
        this.getOrder = getOrder;
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SPOTS, allEntries = true)
    public ResponseEntity<OrderResponse> createOrder(final CreateOrderRequest input) {
        final var output = HttpResults.require(createOrder.execute(
                CreateOrderCommand.with(input.customerId(), input.spotIds())));
        return ResponseEntity.created(URI.create("/orders/" + output.orderId()))
                .body(OrderResponse.from(output));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SPOTS, allEntries = true)
    public ResponseEntity<PayOrderResponse> payOrder(final String id) {
        final var output = HttpResults.require(payOrder.execute(PayOrderCommand.with(id)));
        return ResponseEntity.ok(PayOrderResponse.from(output));
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SPOTS, allEntries = true)
    public ResponseEntity<OrderResponse> cancelOrder(final String id) {
        final var output = HttpResults.require(cancelOrder.execute(CancelOrderCommand.with(id)));
        return ResponseEntity.ok(OrderResponse.from(output));
    }

    @Override
    public ResponseEntity<OrderResponse> getOrder(final String id) {
        return ResponseEntity.ok(OrderResponse.from(HttpResults.require(getOrder.execute(id))));
    }
}
