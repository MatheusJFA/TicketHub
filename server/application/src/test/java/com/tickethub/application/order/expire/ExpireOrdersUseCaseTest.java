package com.tickethub.application.order.expire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderItem;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.shared.Location;
import com.tickethub.domain.shared.Money;

@DisplayName("Expire orders use case")
class ExpireOrdersUseCaseTest extends UseCaseTest {

    private static final Currency BRL = Currency.getInstance("BRL");
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-20T12:00:00Z"), ZoneOffset.UTC);

    private final OrderGateway orderGateway = mock(OrderGateway.class);
    private final SpotGateway spotGateway = mock(SpotGateway.class);
    private final DefaultExpireOrdersUseCase useCase =
            new DefaultExpireOrdersUseCase(orderGateway, spotGateway, CLOCK);

    @Override
    protected List<Object> getMocks() {
        return List.of(orderGateway, spotGateway);
    }

    @Test
    @DisplayName("Given elapsed orders, when execute, then expires and releases spots")
    void givenElapsedOrders_whenExecute_thenExpiresAndReleases() {
        final var spot = Spot.create(Location.create("A1"));
        spot.reserve();
        final var order = Order.create(CustomerID.generate(),
                List.of(OrderItem.of(spot.getId(), Money.create(new BigDecimal("50.00"), BRL))),
                TTL, Clock.fixed(CLOCK.instant().minus(TTL).minusSeconds(60), ZoneOffset.UTC));
        when(orderGateway.findPendingExpired(CLOCK.instant())).thenReturn(List.of(order));
        when(spotGateway.findById(spot.getId())).thenReturn(Optional.of(spot));
        when(spotGateway.update(any())).thenAnswer(returnsFirstArg());
        when(orderGateway.update(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute().getRight();

        assertNotNull(output);
        assertEquals(1, output.expired());
        assertEquals(List.of(order.getId().getValue()), output.orderIds());
        verify(orderGateway, times(1)).findPendingExpired(any());
        verify(spotGateway, times(1)).findById(spot.getId());
        verify(orderGateway, times(1)).update(any());
        verify(spotGateway, times(1)).update(any());
        assertFalse(spot.isReserved());
    }

    @Test
    @DisplayName("Given no elapsed orders, when execute, then expires nothing")
    void givenNoElapsedOrders_whenExecute_thenExpiresNothing() {
        when(orderGateway.findPendingExpired(CLOCK.instant())).thenReturn(List.of());

        final var output = useCase.execute().getRight();

        assertEquals(0, output.expired());
        verify(orderGateway, times(1)).findPendingExpired(any());
        verify(orderGateway, times(0)).update(any());
    }
}
