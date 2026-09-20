package com.tickethub.infrastructure.authentication.models;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank(message = "must not be blank") String refreshToken) {
}
