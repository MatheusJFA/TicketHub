package com.tickethub.domain.authentication;

import java.util.Optional;

public interface AuthAccountGateway {
    Optional<AuthAccount> findByIdentifier(String identifier);
}
