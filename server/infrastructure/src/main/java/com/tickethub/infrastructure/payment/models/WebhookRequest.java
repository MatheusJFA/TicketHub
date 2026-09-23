package com.tickethub.infrastructure.payment.models;

import jakarta.validation.constraints.NotBlank;

/**
 * Provider webhook body: the charge identity and its new status
 * ({@code PAID} or {@code FAILED}). Called without authentication, like any
 * PSP callback; provider signature verification is future work.
 */
public record WebhookRequest(
        @NotBlank String chargeId, @NotBlank String status) {}
