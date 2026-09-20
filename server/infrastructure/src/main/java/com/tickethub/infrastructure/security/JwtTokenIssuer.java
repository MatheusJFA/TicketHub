package com.tickethub.infrastructure.security;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import static java.util.Objects.nonNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import com.tickethub.domain.authentication.IssuedToken;
import com.tickethub.domain.authentication.TokenIssuer;

@Component
public class JwtTokenIssuer implements TokenIssuer {

    private final SecurityProperties properties;
    private final JwtEncoder jwtEncoder;

    public JwtTokenIssuer(final SecurityProperties properties, final JwtEncoder jwtEncoder) {
        this.properties = Objects.requireNonNull(properties, "'properties' should not be null");
        this.jwtEncoder = Objects.requireNonNull(jwtEncoder, "'jwtEncoder' should not be null");
    }

    @Override
    public IssuedToken issueAccess(final String subject, final List<String> authorities, final String ownerId) {
        final Instant now = Instant.now();
        final long expirationMinutes = properties.getJwt().getExpirationMinutes();
        final var claims = JwtClaimsSet.builder()
                .issuer("tickethub")
                .issuedAt(now)
                .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
                .subject(subject)
                .claim("authorities", List.copyOf(emptyIfNull(authorities)));
        if (nonNull(ownerId)) {
            claims.claim("ownerId", ownerId);
        }
        final String token = jwtEncoder
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims.build()))
                .getTokenValue();
        return new IssuedToken(token, expirationMinutes * 60);
    }
}
