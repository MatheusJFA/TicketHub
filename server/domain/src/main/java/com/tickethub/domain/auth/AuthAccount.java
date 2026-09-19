package com.tickethub.domain.auth;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public record AuthAccount(String subject, String passwordHash, List<String> authorities, String ownerId) {

    public AuthAccount {
        if (StringUtils.isBlank(subject)) {
            throw new IllegalArgumentException("'subject' should not be null or blank");
        }
        if (StringUtils.isBlank(passwordHash)) {
            throw new IllegalArgumentException("'passwordHash' should not be null or blank");
        }
        authorities = CollectionUtils.isEmpty(authorities) ? List.of() : List.copyOf(authorities);
        ownerId = StringUtils.isBlank(ownerId) ? null : ownerId;
    }
}
