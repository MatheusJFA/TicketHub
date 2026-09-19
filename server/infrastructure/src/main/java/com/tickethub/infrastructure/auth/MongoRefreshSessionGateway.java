package com.tickethub.infrastructure.auth;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.tickethub.domain.auth.RefreshSession;
import com.tickethub.domain.auth.RefreshSessionGateway;
import com.tickethub.infrastructure.auth.persistence.RefreshSessionDocument;
import com.tickethub.infrastructure.auth.persistence.RefreshSessionRepository;

@Component
public class MongoRefreshSessionGateway implements RefreshSessionGateway {

    private final MongoTemplate mongoTemplate;
    private final RefreshSessionRepository repository;

    public MongoRefreshSessionGateway(final MongoTemplate mongoTemplate,
            final RefreshSessionRepository repository) {
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
        this.repository = Objects.requireNonNull(repository, "'repository' should not be null");
    }

    @Override
    public RefreshSession save(final RefreshSession session) {
        return mongoTemplate.save(RefreshSessionDocument.from(session), RefreshSessionDocument.COLLECTION)
                .toDomain();
    }

    @Override
    public Optional<RefreshSession> findByTokenHash(final String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(RefreshSessionDocument::toDomain);
    }

    @Override
    public List<RefreshSession> findByFamilyId(final String familyId) {
        return repository.findByFamilyId(familyId).stream().map(RefreshSessionDocument::toDomain).toList();
    }
}
