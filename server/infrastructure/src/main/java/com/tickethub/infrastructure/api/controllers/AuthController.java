package com.tickethub.infrastructure.api.controllers;

import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.tickethub.infrastructure.api.AuthAPI;
import com.tickethub.infrastructure.auth.models.LoginRequest;
import com.tickethub.infrastructure.auth.models.TokenResponse;
import com.tickethub.infrastructure.security.AuthService;

@RestController
public class AuthController implements AuthAPI {

    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = Objects.requireNonNull(authService, "'authService' should not be null");
    }

    @Override
    public ResponseEntity<TokenResponse> login(final LoginRequest input) {
        return ResponseEntity.ok(authService.login(input.username(), input.password()));
    }
}
