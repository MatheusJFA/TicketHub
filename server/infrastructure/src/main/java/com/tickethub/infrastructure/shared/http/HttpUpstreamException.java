package com.tickethub.infrastructure.shared.http;

import com.tickethub.infrastructure.exception.InfrastructureException;

public class HttpUpstreamException extends InfrastructureException {

    private final String provider;
    private final int status;

    public HttpUpstreamException(final String provider, final int status, final String message) {
        super("[%s] upstream failure status=%d: %s".formatted(provider, status, message));
        this.provider = provider;
        this.status = status;
    }

    public HttpUpstreamException(final String provider, final Exception cause) {
        super("[%s] upstream failure: %s".formatted(provider, cause.getMessage()), cause);
        this.provider = provider;
        this.status = -1;
    }

    public String getProvider() {
        return provider;
    }

    public int getStatus() {
        return status;
    }
}
