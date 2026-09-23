package com.tickethub.infrastructure.security;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record SecurityUser(String username, String passwordHash, Set<Role> roles, String ownerId) {

    public SecurityUser {
        if (isBlank(username)) {
            throw new IllegalArgumentException("'username' should not be null or blank");
        }
        requireNonNull(passwordHash, "'passwordHash' should not be null");
        roles = isEmpty(roles) ? Set.of(Role.CUSTOMER) : Set.copyOf(roles);
        ownerId = defaultIfBlank(ownerId, null);
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
