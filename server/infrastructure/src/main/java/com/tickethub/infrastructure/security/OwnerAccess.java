package com.tickethub.infrastructure.security;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.springframework.stereotype.Component;

@Component("ownerAccess")
public class OwnerAccess {

    public boolean isSelfOrAdmin(final String id) {
        if (SecuritySupport.isAdmin()) {
            return true;
        }
        return isNotBlank(id) && SecuritySupport.ownerId().map(id::equals).orElse(false);
    }
}
