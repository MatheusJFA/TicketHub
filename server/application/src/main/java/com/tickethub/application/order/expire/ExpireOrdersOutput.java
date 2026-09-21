package com.tickethub.application.order.expire;

import java.util.List;

public record ExpireOrdersOutput(int expired, List<String> orderIds) {
    public static ExpireOrdersOutput from(final List<String> orderIds) {
        return new ExpireOrdersOutput(orderIds.size(), List.copyOf(orderIds));
    }
}
