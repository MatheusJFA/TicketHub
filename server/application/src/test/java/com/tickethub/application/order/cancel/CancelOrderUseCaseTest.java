package com.tickethub.application.order.cancel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.order.OrderItem;
import com.tickethub.domain.core.order.OrderStatus;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.shared.Location;
import com.tickethub.domain.shared.Money;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Cancel order use case")
class CancelOrderUseCaseTest extends UseCaseTest {

    private static final Currency BRL = Currency.getInstance("BRL");

    private final OrderGateway orderGateway = mock(OrderGateway.class);
    private final SpotGateway spotGateway = mock(SpotGateway.class);
    private final DefaultCancelOrderUseCase useCase = new DefaultCancelOrderUseCase(orderGateway, spotGateway);

    @Override
    protected List<Object> getMocks() {
        return List.of(orderGateway, spotGateway);
    }

    private Order givenOrder(final Spot spot) {
        final var order = Order.create(
                CustomerID.generate(),
                List.of(OrderItem.of(spot.getId(), Money.create(new BigDecimal("50.00"), BRL))),
                Duration.ofMinutes(15));
        when(orderGateway.findById(order.getId())).thenReturn(Optional.of(order));
        when(spotGateway.findById(spot.getId())).thenReturn(Optional.of(spot));
        when(spotGateway.update(any())).thenAnswer(returnsFirstArg());
        when(orderGateway.update(any())).thenAnswer(returnsFirstArg());
        spot.reserve();
        return order;
    }

    @Test
    @DisplayName("Given pending order, when execute, then cancels and releases spots")
    void givenPendingOrder_whenExecute_thenCancelsAndReleases() {
        final var spot = Spot.create(Location.create("A1"));
        final var order = givenOrder(spot);

        final var output = useCase.execute(CancelOrderCommand.with(order.getId().getValue()))
                .getRight();

        assertNotNull(output);
        assertEquals(OrderStatus.CANCELLED.name(), output.status());
        verify(orderGateway, times(1)).findById(order.getId());
        verify(spotGateway, times(1)).findById(spot.getId());
        verify(spotGateway, times(1)).update(any());
        verify(orderGateway, times(1)).update(any());
        assertFalse(spot.isReserved());
    }

    @Test
    @DisplayName("Given paid order, when execute, then refuses without changes")
    void givenPaidOrder_whenExecute_thenRefuses() {
        final var spot = Spot.create(Location.create("A1"));
        final var order = givenOrder(spot);
        order.markAsPaid();

        final var notification = useCase.execute(
                        CancelOrderCommand.with(order.getId().getValue()))
                .getLeft();

        assertEquals(
                "Illegal order transition from PAID to CANCELLED",
                notification.firstError().message());
        verify(orderGateway, times(1)).findById(order.getId());
        verify(orderGateway, times(0)).update(any());
    }

    @Test
    @DisplayName("Given unknown order, when execute, then returns not found")
    void givenUnknownOrder_whenExecute_thenReturnsNotFound() {
        final var orderId = OrderID.generate();
        when(orderGateway.findById(orderId)).thenReturn(Optional.empty());

        final var notification =
                useCase.execute(CancelOrderCommand.with(orderId.getValue())).getLeft();

        assertEquals(
                "Order not found: " + orderId.getValue(),
                notification.firstError().message());
        verify(orderGateway, times(1)).findById(orderId);
    }
}
