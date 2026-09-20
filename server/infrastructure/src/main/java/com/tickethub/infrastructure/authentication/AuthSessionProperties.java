package com.tickethub.infrastructure.authentication;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tickethub.auth")
public class AuthSessionProperties {

    private Duration refreshTtl = Duration.ofDays(7);
    private int loginRateLimitPerMinute = 10;

    public Duration getRefreshTtl() {
        return refreshTtl;
    }

    public void setRefreshTtl(final Duration refreshTtl) {
        this.refreshTtl = refreshTtl;
    }

    public int getLoginRateLimitPerMinute() {
        return loginRateLimitPerMinute;
    }

    public void setLoginRateLimitPerMinute(final int loginRateLimitPerMinute) {
        this.loginRateLimitPerMinute = loginRateLimitPerMinute;
    }
}
