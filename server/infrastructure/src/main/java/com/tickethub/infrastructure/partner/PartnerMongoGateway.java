package com.tickethub.infrastructure.partner;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.shared.Email;
import com.tickethub.infrastructure.partner.persistence.PartnerDocument;
import com.tickethub.infrastructure.partner.persistence.PartnerRepository;
import com.tickethub.infrastructure.shared.persistence.MongoGatewaySupport;

@Component
public class PartnerMongoGateway implements PartnerGateway {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "cnpj", "createdAt", "updatedAt");

    private final MongoTemplate mongoTemplate;
    private final PartnerRepository repository;

    public PartnerMongoGateway(final MongoTemplate mongoTemplate, final PartnerRepository repository) {
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
        this.repository = Objects.requireNonNull(repository, "'repository' should not be null");
    }

    @Override
    public Partner create(final Partner partner) {
        try {
            return mongoTemplate.insert(PartnerDocument.from(partner), PartnerDocument.COLLECTION).toDomain();
        } catch (final DuplicateKeyException e) {
            throw duplicateKey(e);
        }
    }

    @Override
    public void deleteById(final PartnerID id) {
        repository.deleteById(id.getValue());
    }

    @Override
    public Optional<Partner> findById(final PartnerID id) {
        return repository.findById(id.getValue()).map(PartnerDocument::toDomain);
    }

    @Override
    public Optional<Partner> findByEmail(final Email email) {
        return repository.findByEmail(email.getValue()).map(PartnerDocument::toDomain);
    }

    @Override
    public Partner update(final Partner partner) {
        try {
            return mongoTemplate.save(PartnerDocument.from(partner), PartnerDocument.COLLECTION).toDomain();
        } catch (final DuplicateKeyException e) {
            throw duplicateKey(e);
        }
    }

    @Override
    public Pagination<Partner> findAll(final SearchQuery query) {
        final var mongoQuery = MongoGatewaySupport.searchQuery(query, "name", "cnpj");
        return MongoGatewaySupport.paginate(mongoTemplate, mongoQuery, PartnerDocument.class,
                PartnerDocument.COLLECTION, query, SORTABLE_FIELDS, PartnerDocument::toDomain);
    }

    private static DomainException duplicateKey(final DuplicateKeyException e) {
        if (e.getMessage() != null && e.getMessage().contains("uq_partners_cnpj")) {
            return new DomainException("'cnpj' already in use");
        }
        if (e.getMessage() != null && e.getMessage().contains("uq_partners_email")) {
            return new DomainException("'email' already in use");
        }
        throw e;
    }
}
