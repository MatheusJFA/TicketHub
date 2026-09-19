package com.tickethub.infrastructure.auth.models;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank(message = "must not be blank") String refreshToken) {
}
