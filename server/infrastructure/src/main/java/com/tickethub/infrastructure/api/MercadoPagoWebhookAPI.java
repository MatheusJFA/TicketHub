package com.tickethub.infrastructure.api;

import com.tickethub.infrastructure.payment.models.ConfirmPaymentResponse;
import com.tickethub.infrastructure.payment.models.MercadoPagoNotification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(value = "/payments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Payments")
public interface MercadoPagoWebhookAPI {
    @PostMapping(value = "/mercadopago", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Mercado Pago webhook",
            description =
                    "Real provider callback: verifies the x-signature, then reads the payment status from the MP API (never trusted from the callback). Non-payment topics are acknowledged without effect")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Webhook verified and processed, or non-payment topic ignored"),
        @ApiResponse(responseCode = "401", description = "Missing, malformed, stale or mismatched x-signature"),
        @ApiResponse(responseCode = "404", description = "The payment was not found for the supplied identifier"),
        @ApiResponse(
                responseCode = "422",
                description = "The charge status is invalid or the reservation expired before confirmation"),
        @ApiResponse(
                responseCode = "500",
                description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(
                responseCode = "503",
                description =
                        "The operation is unavailable because its required service dependencies are not configured")
    })
    ResponseEntity<ConfirmPaymentResponse> mercadoPagoWebhook(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestParam(value = "type", required = false) String type,
            @RequestBody(required = false) MercadoPagoNotification body);
}
