package com.tickethub.domain.authentication;

import java.util.Optional;

public interface AccessTokenInspector {
    Optional<AccessTokenIdentity> inspect(String token);
}
