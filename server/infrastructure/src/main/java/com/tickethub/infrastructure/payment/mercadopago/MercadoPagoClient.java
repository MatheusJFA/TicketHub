package com.tickethub.infrastructure.payment.mercadopago;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tickethub.infrastructure.shared.http.BaseHttpClient;
import com.tickethub.infrastructure.shared.http.HttpUpstreamException;

/**
 * Thin REST client for the Mercado Pago Payments API ({@code /v1/payments}).
 * Authentication is applied as a default {@code Authorization} header on the
 * injected {@link RestClient}; this class only maps requests and responses.
 */
public class MercadoPagoClient extends BaseHttpClient {

    public MercadoPagoClient(final RestClient restClient) {
        super(restClient, "mercadopago");
    }

    public PaymentResponse createPixPayment(final BigDecimal amount, final String description,
            final String externalReference, final String payerEmail, final String notificationUrl) {
        requireNonNull(amount, "'amount' should not be null");
        requireNonNull(externalReference, "'externalReference' should not be null");
        requireNonNull(payerEmail, "'payerEmail' should not be null");
        try {
            final var body = new java.util.HashMap<String, Object>(Map.of(
                    "transaction_amount", amount,
                    "description", description,
                    "payment_method_id", "pix",
                    "payer", Map.of("email", payerEmail),
                    "external_reference", externalReference));
            if (notificationUrl != null && !notificationUrl.isBlank()) {
                body.put("notification_url", notificationUrl);
            }
            final var response = restClient.post()
                    .uri("/v1/payments")
                    .header("X-Idempotency-Key", externalReference)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(PaymentResponse.class);
            return requireNonNull(response.getBody(),
                    "'response body' should not be null");
        } catch (final RestClientException e) {
            throw new HttpUpstreamException(provider, e);
        }
    }

    public PaymentResponse getPayment(final String paymentId) {
        requireNonNull(paymentId, "'paymentId' should not be null");
        return getRequired("/v1/payments/{id}", Map.of("id", paymentId), PaymentResponse.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentResponse(
            Long id,
            String status,
            @JsonProperty("transaction_amount") BigDecimal transactionAmount,
            @JsonProperty("currency_id") String currencyId,
            @JsonProperty("external_reference") String externalReference,
            @JsonProperty("point_of_interaction") PointOfInteraction pointOfInteraction) {

        public String qrCode() {
            if (pointOfInteraction == null || pointOfInteraction.transactionData() == null) {
                return null;
            }
            return pointOfInteraction.transactionData().qrCode();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PointOfInteraction(
            @JsonProperty("transaction_data") TransactionData transactionData) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TransactionData(
            @JsonProperty("qr_code") String qrCode,
            @JsonProperty("qr_code_base64") String qrCodeBase64) {
    }
}
