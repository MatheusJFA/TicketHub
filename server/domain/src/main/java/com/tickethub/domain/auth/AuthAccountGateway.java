package com.tickethub.domain.auth;

import java.util.Optional;

public interface AuthAccountGateway {
    Optional<AuthAccount> findByIdentifier(String identifier);
}
