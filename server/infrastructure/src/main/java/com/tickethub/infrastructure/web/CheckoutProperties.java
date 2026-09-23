package com.tickethub.infrastructure.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tickethub.checkout")
public class CheckoutProperties {

    private int orderRateLimitPerMinute = 30;

    public int getOrderRateLimitPerMinute() {
        return orderRateLimitPerMinute;
    }

    public void setOrderRateLimitPerMinute(final int orderRateLimitPerMinute) {
        this.orderRateLimitPerMinute = orderRateLimitPerMinute;
    }
}
