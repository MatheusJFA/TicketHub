package com.tickethub.infrastructure.api.controllers;

import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.tickethub.application.auth.login.LoginCommand;
import com.tickethub.application.auth.login.LoginUseCase;
import com.tickethub.application.auth.logout.LogoutCommand;
import com.tickethub.application.auth.logout.LogoutUseCase;
import com.tickethub.application.auth.refresh.RefreshTokenCommand;
import com.tickethub.application.auth.refresh.RefreshTokenUseCase;
import com.tickethub.infrastructure.api.AuthAPI;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.auth.models.LoginRequest;
import com.tickethub.infrastructure.auth.models.RefreshRequest;
import com.tickethub.infrastructure.auth.models.SessionResponse;

@RestController
public class AuthController implements AuthAPI {

    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(final LoginUseCase loginUseCase, final RefreshTokenUseCase refreshTokenUseCase,
            final LogoutUseCase logoutUseCase) {
        this.loginUseCase = Objects.requireNonNull(loginUseCase, "'loginUseCase' should not be null");
        this.refreshTokenUseCase = Objects.requireNonNull(refreshTokenUseCase, "'refreshTokenUseCase' should not be null");
        this.logoutUseCase = Objects.requireNonNull(logoutUseCase, "'logoutUseCase' should not be null");
    }

    @Override
    public ResponseEntity<SessionResponse> login(final LoginRequest input) {
        final var output = HttpResults.require(
                loginUseCase.execute(new LoginCommand(input.identifier(), input.password())));
        return ResponseEntity.ok(SessionResponse.from(output));
    }

    @Override
    public ResponseEntity<SessionResponse> refresh(final RefreshRequest input) {
        final var output = HttpResults.require(
                refreshTokenUseCase.execute(new RefreshTokenCommand(input.refreshToken())));
        return ResponseEntity.ok(SessionResponse.from(output));
    }

    @Override
    public ResponseEntity<Void> logout(final RefreshRequest input) {
        HttpResults.requireEmpty(logoutUseCase.execute(new LogoutCommand(input.refreshToken())));
        return ResponseEntity.noContent().build();
    }
}
