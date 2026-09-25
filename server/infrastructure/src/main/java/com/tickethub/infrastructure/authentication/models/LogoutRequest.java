package com.tickethub.infrastructure.authentication.models;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "must not be blank") String refreshToken, String accessToken) {}
