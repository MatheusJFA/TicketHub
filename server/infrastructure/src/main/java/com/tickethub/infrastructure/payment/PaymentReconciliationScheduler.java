package com.tickethub.infrastructure.payment;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.springframework.cache.Cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tickethub.application.payment.reconcile.ReconcileOrdersUseCase;
import com.tickethub.infrastructure.cache.TickethubCacheProperties;

/**
 * Periodically reconciles charged orders past their TTL whose webhook never
 * arrived: in-time approvals settle, late captures refund, the rest expires.
 * Fixed delay (never overlapping). Clears the spots cache only when the run
 * changed availability.
 */
@Component
public class PaymentReconciliationScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentReconciliationScheduler.class);

    private final ReconcileOrdersUseCase reconcileOrders;
    private final ObjectProvider<CacheManager> cacheManager;

    public PaymentReconciliationScheduler(final ReconcileOrdersUseCase reconcileOrders,
            final ObjectProvider<CacheManager> cacheManager) {
        this.reconcileOrders = requireNonNull(reconcileOrders, "'reconcileOrders' should not be null");
        this.cacheManager = requireNonNull(cacheManager, "'cacheManager' should not be null");
    }

    @Scheduled(fixedDelayString = "${tickethub.payment.reconcile-interval:2m}")
    public void reconcile() {
        final var result = reconcileOrders.execute();
        if (result.isLeft()) {
            LOG.warn("Payment reconciliation failed error={}", result.getLeft().getErrors());
            return;
        }
        final var output = result.getRight();
        if (output.isEmpty()) {
            return;
        }
        LOG.info("Payment reconciliation settled={} refunded={} expired={}",
                output.settled().size(), output.refunded().size(), output.expired().size());
        Optional.ofNullable(cacheManager.getIfAvailable()).ifPresent(manager -> {
            for (final var name : new String[] { TickethubCacheProperties.SHOWS,
                    TickethubCacheProperties.SECTIONS, TickethubCacheProperties.SPOTS }) {
                Optional.ofNullable(manager.getCache(name)).ifPresent(Cache::clear);
            }
        });
    }
}
