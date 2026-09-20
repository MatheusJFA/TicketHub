package com.tickethub.domain.auth;

import static java.util.List.copyOf;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;

public record AuthAccount(String subject, String passwordHash, List<String> authorities, String ownerId) {

    public AuthAccount {
        if (isBlank(subject)) {
            throw new IllegalArgumentException("'subject' should not be null or blank");
        }
        if (isBlank(passwordHash)) {
            throw new IllegalArgumentException("'passwordHash' should not be null or blank");
        }
        authorities = copyOf(emptyIfNull(authorities));
        ownerId = defaultIfBlank(ownerId, null);
    }
}
