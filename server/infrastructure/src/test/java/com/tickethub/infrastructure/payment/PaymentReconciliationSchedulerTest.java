package com.tickethub.infrastructure.payment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickethub.application.Either;
import com.tickethub.application.payment.reconcile.ReconcileOrdersOutput;
import com.tickethub.application.payment.reconcile.ReconcileOrdersUseCase;
import com.tickethub.infrastructure.cache.TickethubCacheProperties;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@DisplayName("Payment reconciliation scheduler")
class PaymentReconciliationSchedulerTest {

    private record Fixture(PaymentReconciliationScheduler scheduler, ReconcileOrdersUseCase useCase, Cache spots) {}

    private Fixture fixture() {
        final var useCase = mock(ReconcileOrdersUseCase.class);
        final var spots = mock(Cache.class);
        final var manager = mock(CacheManager.class);
        when(manager.getCache(TickethubCacheProperties.SPOTS)).thenReturn(spots);
        @SuppressWarnings("unchecked")
        final var provider = (ObjectProvider<CacheManager>) mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(manager);
        return new Fixture(new PaymentReconciliationScheduler(useCase, provider), useCase, spots);
    }

    @Test
    @DisplayName("Given settled orders, when reconcile, then clears spots cache")
    void givenSettledOrders_whenReconcile_thenClearsSpotsCache() {
        final var fixture = fixture();
        when(fixture.useCase().execute())
                .thenReturn(Either.right(ReconcileOrdersOutput.from(List.of("order-1"), List.of(), List.of())));

        fixture.scheduler().reconcile();

        verify(fixture.useCase()).execute();
        verify(fixture.spots()).clear();
    }

    @Test
    @DisplayName("Given empty run, when reconcile, then keeps cache")
    void givenEmptyRun_whenReconcile_thenKeepsCache() {
        final var fixture = fixture();
        when(fixture.useCase().execute())
                .thenReturn(Either.right(ReconcileOrdersOutput.from(List.of(), List.of(), List.of())));

        fixture.scheduler().reconcile();

        verify(fixture.useCase()).execute();
        verify(fixture.spots(), never()).clear();
    }
}
