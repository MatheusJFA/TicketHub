package com.tickethub.infrastructure.authentication.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefreshSessionRepository extends MongoRepository<RefreshSessionDocument, String> {

    Optional<RefreshSessionDocument> findByTokenHash(String tokenHash);

    List<RefreshSessionDocument> findByFamilyId(String familyId);
}
