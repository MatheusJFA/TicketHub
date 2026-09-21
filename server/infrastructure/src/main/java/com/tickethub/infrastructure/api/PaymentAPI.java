package com.tickethub.infrastructure.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tickethub.infrastructure.payment.models.ConfirmPaymentResponse;
import com.tickethub.infrastructure.payment.models.WebhookRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RequestMapping(value = "/payments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Payments")
public interface PaymentAPI {
    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Payment Webhook",
            description = "Provider callback: on PAID settles the order and issues one ticket per item; on FAILED the order stays PENDING; unknown charges return 404")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Webhook processed; returns the order status"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The charge was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The charge status is invalid or the reservation expired before confirmation"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    ResponseEntity<ConfirmPaymentResponse> paymentWebhook(@Valid @RequestBody WebhookRequest input);
}
