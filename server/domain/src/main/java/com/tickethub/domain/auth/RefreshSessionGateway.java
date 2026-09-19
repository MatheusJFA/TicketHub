package com.tickethub.domain.auth;

import java.util.List;
import java.util.Optional;

public interface RefreshSessionGateway {
    RefreshSession save(RefreshSession session);

    Optional<RefreshSession> findByTokenHash(String tokenHash);

    List<RefreshSession> findByFamilyId(String familyId);
}
