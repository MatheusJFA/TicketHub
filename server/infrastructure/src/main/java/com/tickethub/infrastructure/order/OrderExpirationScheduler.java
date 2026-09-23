package com.tickethub.infrastructure.order;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.order.expire.ExpireOrdersUseCase;
import com.tickethub.infrastructure.cache.TickethubCacheProperties;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically expires abandoned PENDING orders and releases their spots.
 * Fixed delay (never overlapping): a slow sweep postpones the next one.
 * The spots cache is cleared only when the sweep actually freed seats.
 */
@Component
public class OrderExpirationScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(OrderExpirationScheduler.class);

    private final ExpireOrdersUseCase expireOrders;
    private final ObjectProvider<CacheManager> cacheManager;

    public OrderExpirationScheduler(
            final ExpireOrdersUseCase expireOrders, final ObjectProvider<CacheManager> cacheManager) {
        this.expireOrders = requireNonNull(expireOrders, "'expireOrders' should not be null");
        this.cacheManager = requireNonNull(cacheManager, "'cacheManager' should not be null");
    }

    @Scheduled(fixedDelayString = "${tickethub.orders.expire-interval:1m}")
    public void sweep() {
        final var result = expireOrders.execute();
        if (result.isLeft()) {
            LOG.warn("Order expiration sweep failed error={}", result.getLeft().getErrors());
            return;
        }
        final var output = result.getRight();
        if (output.expired() == 0) {
            return;
        }
        LOG.info("Order expiration sweep expired={} orderIds={}", output.expired(), output.orderIds());
        Optional.ofNullable(cacheManager.getIfAvailable())
                .map(manager -> manager.getCache(TickethubCacheProperties.SPOTS))
                .ifPresent(Cache::clear);
    }
}
