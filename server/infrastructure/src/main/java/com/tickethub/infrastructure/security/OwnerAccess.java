package com.tickethub.infrastructure.security;

import org.springframework.stereotype.Component;

@Component("ownerAccess")
public class OwnerAccess {

    public boolean isSelfOrAdmin(final String id) {
        if (SecuritySupport.isAdmin()) {
            return true;
        }
        return id != null && !id.isBlank() && SecuritySupport.ownerId().map(id::equals).orElse(false);
    }
}
