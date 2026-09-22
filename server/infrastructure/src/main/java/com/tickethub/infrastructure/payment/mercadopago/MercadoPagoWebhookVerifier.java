package com.tickethub.infrastructure.payment.mercadopago;

import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates Mercado Pago webhook origin from the {@code x-signature} header
 * ({@code ts=<ts>,v1=<hex>}) plus {@code x-request-id} and the {@code data.id}
 * query param. Manifest, from the MP docs, is exact, including the trailing
 * semicolons: {@code id:&lt;dataId&gt;;request-id:&lt;requestId&gt;;ts:&lt;ts&gt;;}
 * Segments with missing values are omitted. Comparison is constant-time and
 * the timestamp must fall inside the tolerance (seconds or milliseconds).
 */
public class MercadoPagoWebhookVerifier {

    private static final Logger LOG = LoggerFactory.getLogger(MercadoPagoWebhookVerifier.class);

    private final String secret;
    private final Duration tolerance;
    private final Clock clock;

    public MercadoPagoWebhookVerifier(final String secret, final Duration tolerance) {
        this(secret, tolerance, Clock.systemUTC());
    }

    public MercadoPagoWebhookVerifier(final String secret, final Duration tolerance, final Clock clock) {
        this.secret = requireNonNull(secret, "'secret' should not be null");
        this.tolerance = requireNonNull(tolerance, "'tolerance' should not be null");
        this.clock = requireNonNull(clock, "'clock' should not be null");
    }

    public void verify(final String xSignature, final String xRequestId, final String dataId) {
        if (secret.isBlank()) {
            throw new InvalidWebhookSignatureException("Mercado Pago webhook secret is not configured");
        }
        if (xSignature == null || dataId == null || dataId.isBlank()) {
            throw new InvalidWebhookSignatureException("Missing webhook signature or payment id");
        }
        String ts = null;
        String hash = null;
        for (final String part : xSignature.split(",")) {
            final int separator = part.indexOf('=');
            if (separator < 0) {
                continue;
            }
            final String key = part.substring(0, separator).trim();
            final String value = part.substring(separator + 1).trim();
            if ("ts".equals(key)) {
                ts = value;
            } else if ("v1".equals(key)) {
                hash = value;
            }
        }
        if (ts == null || hash == null) {
            throw new InvalidWebhookSignatureException("Malformed webhook signature");
        }
        checkFreshness(ts);
        final var manifest = new StringBuilder();
        manifest.append("id:").append(normalize(dataId)).append(';');
        if (xRequestId != null && !xRequestId.isBlank()) {
            manifest.append("request-id:").append(xRequestId).append(';');
        }
        manifest.append("ts:").append(ts).append(';');
        final var expected = hmacHex(manifest.toString());
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                hash.getBytes(StandardCharsets.UTF_8))) {
            LOG.warn("Mercado Pago signature mismatch dataId={}", dataId);
            throw new InvalidWebhookSignatureException("Invalid webhook signature");
        }
    }

    private void checkFreshness(final String ts) {
        final long raw;
        try {
            raw = Long.parseLong(ts);
        } catch (final NumberFormatException e) {
            throw new InvalidWebhookSignatureException("Malformed webhook timestamp");
        }
        final var sentAt = Instant.ofEpochMilli(raw < 1_000_000_000_000L ? raw * 1000 : raw);
        if (sentAt.plus(tolerance).isBefore(clock.instant())) {
            throw new InvalidWebhookSignatureException("Stale webhook notification");
        }
    }

    private static String normalize(final String dataId) {
        final var trimmed = dataId.trim();
        return trimmed.chars().allMatch(Character::isLetterOrDigit)
                ? trimmed.toLowerCase(Locale.ROOT)
                : trimmed;
    }

    private String hmacHex(final String message) {
        try {
            final var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            final var bytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            final var hex = new StringBuilder(bytes.length * 2);
            for (final byte b : bytes) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (final NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HmacSHA256 is unavailable", e);
        }
    }
}
