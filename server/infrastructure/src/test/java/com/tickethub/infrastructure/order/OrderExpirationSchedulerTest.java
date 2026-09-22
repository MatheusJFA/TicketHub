package com.tickethub.infrastructure.order;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.tickethub.application.Either;
import com.tickethub.application.order.expire.ExpireOrdersOutput;
import com.tickethub.application.order.expire.ExpireOrdersUseCase;
import com.tickethub.domain.validation.Notification;
import com.tickethub.infrastructure.cache.TickethubCacheProperties;

@DisplayName("Order expiration scheduler")
class OrderExpirationSchedulerTest {

    private record Fixture(OrderExpirationScheduler scheduler, ExpireOrdersUseCase useCase,
            Cache spots) {
    }

    private Fixture fixture() {
        final var useCase = mock(ExpireOrdersUseCase.class);
        final var spots = mock(Cache.class);
        final var manager = mock(CacheManager.class);
        when(manager.getCache(TickethubCacheProperties.SPOTS)).thenReturn(spots);
        @SuppressWarnings("unchecked")
        final var provider = (ObjectProvider<CacheManager>) mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(manager);
        return new Fixture(new OrderExpirationScheduler(useCase, provider), useCase, spots);
    }

    @Test
    @DisplayName("Given expired orders, when sweep, then clears spots cache")
    void givenExpiredOrders_whenSweep_thenClearsSpotsCache() {
        final var fixture = fixture();
        when(fixture.useCase().execute())
                .thenReturn(Either.right(ExpireOrdersOutput.from(List.of("order-1"))));

        fixture.scheduler().sweep();

        verify(fixture.useCase()).execute();
        verify(fixture.spots()).clear();
    }

    @Test
    @DisplayName("Given nothing expired, when sweep, then keeps cache")
    void givenNothingExpired_whenSweep_thenKeepsCache() {
        final var fixture = fixture();
        when(fixture.useCase().execute())
                .thenReturn(Either.right(ExpireOrdersOutput.from(List.of())));

        fixture.scheduler().sweep();

        verify(fixture.useCase()).execute();
        verify(fixture.spots(), never()).clear();
    }

    @Test
    @DisplayName("Given sweep failure, when sweep, then keeps cache")
    void givenSweepFailure_whenSweep_thenKeepsCache() {
        final var fixture = fixture();
        when(fixture.useCase().execute()).thenReturn(Either.left(Notification.create()));

        fixture.scheduler().sweep();

        verify(fixture.useCase()).execute();
        verify(fixture.spots(), never()).clear();
    }
}
