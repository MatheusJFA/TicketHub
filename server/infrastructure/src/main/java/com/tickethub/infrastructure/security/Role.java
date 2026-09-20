package com.tickethub.infrastructure.security;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum Role {

    CUSTOMER(EnumSet.of(
            Permission.CUSTOMER_WRITE,
            Permission.CUSTOMER_DELETE)),

    PARTNER(EnumSet.of(
            Permission.PARTNER_WRITE,
            Permission.PARTNER_DELETE,
            Permission.SHOW_CREATE,
            Permission.SHOW_WRITE,
            Permission.SHOW_PUBLISH,
            Permission.SHOW_DELETE,
            Permission.SECTION_WRITE,
            Permission.SECTION_PUBLISH,
            Permission.SECTION_DELETE,
            Permission.SPOT_WRITE,
            Permission.SPOT_PUBLISH,
            Permission.SPOT_DELETE,
            Permission.TICKET_VALIDATE)),

    ADMIN(EnumSet.allOf(Permission.class));

    private final Set<Permission> permissions;

    Role(final Set<Permission> permissions) {
        this.permissions = Collections.unmodifiableSet(permissions);
    }

    public Set<Permission> permissions() {
        return permissions;
    }
}
