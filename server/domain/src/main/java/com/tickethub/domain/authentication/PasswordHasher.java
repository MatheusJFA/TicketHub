package com.tickethub.domain.authentication;
import java.util.Objects;

public interface PasswordHasher {
    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
