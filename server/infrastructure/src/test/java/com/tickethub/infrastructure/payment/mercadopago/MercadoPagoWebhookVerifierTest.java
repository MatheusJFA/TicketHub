package com.tickethub.infrastructure.payment.mercadopago;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("Mercado Pago webhook verifier")
class MercadoPagoWebhookVerifierTest {

    private static final String SECRET = "test_secret_key";
    private static final String DATA_ID = "123456789";
    private static final String REQUEST_ID = "req-abc";
    private static final String TS = "1704908010";
    // HMAC-SHA256(secret, "id:123456789;request-id:req-abc;ts:1704908010;")
    private static final String V1 = "d600632a9073f1582726cd2aa120bb6b4f82f1d906f24cc3a627769e0cdd4b5c";

    private MercadoPagoWebhookVerifier verifier() {
        final var clock = Clock.fixed(Instant.ofEpochSecond(1704908010), ZoneOffset.UTC);
        return new MercadoPagoWebhookVerifier(SECRET, Duration.ofMinutes(5), clock);
    }

    private String header(final String v1) {
        return "ts=" + TS + ",v1=" + v1;
    }

    @Test
    @DisplayName("Given valid signature, when verify, then passes")
    void givenValidSignature_whenVerify_thenPasses() {
        assertDoesNotThrow(() -> verifier().verify(header(V1), REQUEST_ID, DATA_ID));
    }

    @Test
    @DisplayName("Given tampered hash, when verify, then rejects with 401")
    void givenTamperedHash_whenVerify_thenRejects() {
        final var exception = assertThrows(
                InvalidWebhookSignatureException.class,
                () -> verifier().verify(header("0".repeat(64)), REQUEST_ID, DATA_ID));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Given stale timestamp, when verify, then rejects")
    void givenStaleTimestamp_whenVerify_thenRejects() {
        final var clock = Clock.fixed(Instant.ofEpochSecond(1704908010 + 3600), ZoneOffset.UTC);
        final var verifier = new MercadoPagoWebhookVerifier(SECRET, Duration.ofMinutes(5), clock);
        assertThrows(InvalidWebhookSignatureException.class, () -> verifier.verify(header(V1), REQUEST_ID, DATA_ID));
    }

    @Test
    @DisplayName("Given missing parts, when verify, then rejects")
    void givenMissingParts_whenVerify_thenRejects() {
        assertThrows(InvalidWebhookSignatureException.class, () -> verifier().verify("ts=" + TS, REQUEST_ID, DATA_ID));
        assertThrows(InvalidWebhookSignatureException.class, () -> verifier().verify(header(V1), REQUEST_ID, null));
    }

    @Test
    @DisplayName("Given blank secret, when verify, then rejects")
    void givenBlankSecret_whenVerify_thenRejects() {
        final var verifier = new MercadoPagoWebhookVerifier("", Duration.ofMinutes(5));
        assertThrows(InvalidWebhookSignatureException.class, () -> verifier.verify(header(V1), REQUEST_ID, DATA_ID));
    }
}
