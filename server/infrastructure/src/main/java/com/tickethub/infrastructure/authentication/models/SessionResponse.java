package com.tickethub.infrastructure.authentication.models;

import com.tickethub.application.authentication.login.LoginOutput;
import com.tickethub.application.authentication.refresh.RefreshTokenOutput;

public record SessionResponse(String accessToken, String tokenType, long expiresIn, String refreshToken) {

    public static SessionResponse from(final LoginOutput output) {
        return new SessionResponse(output.accessToken(), output.tokenType(), output.expiresIn(),
                output.refreshToken());
    }

    public static SessionResponse from(final RefreshTokenOutput output) {
        return new SessionResponse(output.accessToken(), output.tokenType(), output.expiresIn(),
                output.refreshToken());
    }
}
