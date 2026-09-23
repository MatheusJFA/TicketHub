package com.tickethub.infrastructure.ticket;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.tickethub.domain.core.ticket.TicketSigner;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * HMAC-SHA256 ticket signer. Door scanners accept only QR codes carrying a
 * signature produced with this secret; configure
 * {@code tickethub.tickets.signature-secret} per environment and never reuse
 * the JWT secret here.
 */
@Component
public class HmacTicketSigner implements TicketSigner {

    private final String secret;

    public HmacTicketSigner(@Value("${tickethub.tickets.signature-secret}") final String secret) {
        if (isBlank(secret)) {
            throw new IllegalStateException("tickethub.tickets.signature-secret must not be blank");
        }
        this.secret = secret;
    }

    @Override
    public String sign(final String payload) {
        requireNonNull(payload, "'payload' should not be null");
        try {
            final var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (final NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to sign ticket payload", e);
        }
    }
}
