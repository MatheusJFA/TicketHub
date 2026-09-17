package com.tickethub.infrastructure.security;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record SecurityUser(String username, String passwordHash, Set<Role> roles) {

    public SecurityUser {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("'username' should not be null or blank");
        }
        Objects.requireNonNull(passwordHash, "'passwordHash' should not be null");
        roles = roles == null || roles.isEmpty() ? Set.of(Role.CUSTOMER) : Set.copyOf(roles);
    }

    public List<String> authorities() {
        final var authorities = new LinkedHashSet<String>();
        for (final Role role : roles) {
            authorities.add("ROLE_" + role.name());
            for (final Permission permission : role.permissions()) {
                authorities.add(permission.authority());
            }
        }
        return List.copyOf(authorities);
    }
}
