package com.tickethub.infrastructure.authentication;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.authentication.RefreshSession;
import com.tickethub.domain.authentication.RefreshSessionGateway;
import com.tickethub.infrastructure.authentication.persistence.RefreshSessionDocument;
import com.tickethub.infrastructure.authentication.persistence.RefreshSessionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoRefreshSessionGateway implements RefreshSessionGateway {

    private final MongoTemplate mongoTemplate;
    private final RefreshSessionRepository repository;

    public MongoRefreshSessionGateway(final MongoTemplate mongoTemplate, final RefreshSessionRepository repository) {
        this.mongoTemplate = requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
        this.repository = requireNonNull(repository, "'repository' should not be null");
    }

    @Override
    public RefreshSession save(final RefreshSession session) {
        return mongoTemplate
                .save(RefreshSessionDocument.from(session), RefreshSessionDocument.COLLECTION)
                .toDomain();
    }

    @Override
    public Optional<RefreshSession> findByTokenHash(final String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(RefreshSessionDocument::toDomain);
    }

    @Override
    public List<RefreshSession> findByFamilyId(final String familyId) {
        return repository.findByFamilyId(familyId).stream()
                .map(RefreshSessionDocument::toDomain)
                .toList();
    }
}
