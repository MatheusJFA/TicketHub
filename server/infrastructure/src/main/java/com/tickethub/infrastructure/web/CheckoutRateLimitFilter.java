package com.tickethub.infrastructure.web;

import static java.util.Objects.requireNonNull;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Flood protection for the money paths: order creation and payment. Reads
 * (catalog, seat map) stay unlimited; the per-minute per-client budget is
 * generous on purpose — this stops bots, not buyers.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 11)
public class CheckoutRateLimitFilter extends AbstractRateLimitFilter {

    public CheckoutRateLimitFilter(final CheckoutProperties properties) {
        super("checkout", requireNonNull(properties, "'properties' should not be null")
                .getOrderRateLimitPerMinute());
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        if (!HttpMethod.POST.name().equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        final String uri = request.getRequestURI();
        return !uri.equals("/orders") && !(uri.startsWith("/orders/") && uri.endsWith("/pay"));
    }
}
