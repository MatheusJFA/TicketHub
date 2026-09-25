package com.tickethub.infrastructure.security;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.authentication.AccessTokenIdentity;
import com.tickethub.domain.authentication.AccessTokenInspector;
import java.util.Optional;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessTokenInspector implements AccessTokenInspector {

    private final JwtDecoder jwtDecoder;

    public JwtAccessTokenInspector(final JwtDecoder jwtDecoder) {
        this.jwtDecoder = requireNonNull(jwtDecoder, "'jwtDecoder' should not be null");
    }

    @Override
    public Optional<AccessTokenIdentity> inspect(final String token) {
        try {
            final var jwt = jwtDecoder.decode(token);
            final var tokenId = jwt.getClaimAsString("jti");
            final var expiresAt = jwt.getExpiresAt();
            if (tokenId == null || expiresAt == null) {
                return Optional.empty();
            }
            return Optional.of(new AccessTokenIdentity(tokenId, expiresAt));
        } catch (final RuntimeException e) {
            return Optional.empty();
        }
    }
}
