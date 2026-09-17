package com.tickethub.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

public final class TestTokens {

    private TestTokens() {
    }

    public static String bearer(final String secret, final String... authorities) {
        return bearer(secret, null, authorities);
    }

    public static String bearer(final String secret, final String ownerId, final String... authorities) {
        final SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        final var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        final Instant now = Instant.now();
        final var claims = JwtClaimsSet.builder()
                .issuer("tickethub-test")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject("test-user")
                .claim("authorities", List.of(authorities));
        if (ownerId != null) {
            claims.claim("ownerId", ownerId);
        }
        return encoder
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims.build()))
                .getTokenValue();
    }
}
