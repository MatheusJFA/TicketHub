package com.tickethub.application.order.retrieve.get;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.order.OrderItem;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.shared.Money;

@DisplayName("Get order use case")
class GetOrderUseCaseTest extends UseCaseTest {

    private final OrderGateway orderGateway = mock(OrderGateway.class);
    private final DefaultGetOrderUseCase useCase = new DefaultGetOrderUseCase(orderGateway);

    @Override
    protected List<Object> getMocks() {
        return List.of(orderGateway);
    }

    @Test
    @DisplayName("Given existing order, when execute, then returns order details")
    void givenExistingOrder_whenExecute_thenReturnsDetails() {
        final var order = Order.create(CustomerID.generate(),
                List.of(OrderItem.of(SpotID.generate(),
                        Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL")))),
                Duration.ofMinutes(15));
        when(orderGateway.findById(order.getId())).thenReturn(Optional.of(order));

        final var output = useCase.execute(order.getId().getValue()).getRight();

        assertNotNull(output);
        assertEquals(order.getId().getValue(), output.orderId());
        assertEquals("PENDING", output.status());
        assertEquals(1, output.spotIds().size());
        verify(orderGateway, times(1)).findById(order.getId());
    }

    @Test
    @DisplayName("Given unknown order, when execute, then returns not found")
    void givenUnknownOrder_whenExecute_thenReturnsNotFound() {
        final var orderId = OrderID.generate();
        when(orderGateway.findById(orderId)).thenReturn(Optional.empty());

        final var notification = useCase.execute(orderId.getValue()).getLeft();

        assertEquals("Order not found: " + orderId.getValue(), notification.firstError().message());
        verify(orderGateway, times(1)).findById(orderId);
    }
}
