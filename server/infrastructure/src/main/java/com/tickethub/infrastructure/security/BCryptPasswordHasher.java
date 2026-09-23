package com.tickethub.infrastructure.security;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.authentication.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordHasher(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = requireNonNull(passwordEncoder, "'passwordEncoder' should not be null");
    }

    @Override
    public String hash(final String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(final String rawPassword, final String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
