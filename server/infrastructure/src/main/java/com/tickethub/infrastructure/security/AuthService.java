package com.tickethub.infrastructure.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tickethub.infrastructure.auth.models.TokenResponse;

@Service
public class AuthService {

    private final SecurityProperties properties;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthService(final SecurityProperties properties, final PasswordEncoder passwordEncoder,
            final JwtEncoder jwtEncoder) {
        this.properties = Objects.requireNonNull(properties, "'properties' should not be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "'passwordEncoder' should not be null");
        this.jwtEncoder = Objects.requireNonNull(jwtEncoder, "'jwtEncoder' should not be null");
    }

    public TokenResponse login(final String username, final String password) {
        final SecurityUser user = properties.findByUsername(username)
                .filter(candidate -> passwordEncoder.matches(password, candidate.passwordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        final Instant now = Instant.now();
        final long expirationMinutes = properties.getJwt().getExpirationMinutes();
        final var claims = JwtClaimsSet.builder()
                .issuer("tickethub")
                .issuedAt(now)
                .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
                .subject(user.username())
                .claim("authorities", user.authorities())
                .build();
        final String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return TokenResponse.bearer(token, expirationMinutes * 60);
    }
}
