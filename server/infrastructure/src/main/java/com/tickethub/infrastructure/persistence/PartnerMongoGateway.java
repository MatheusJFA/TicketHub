package com.tickethub.infrastructure.persistence;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;

@Component
public class PartnerMongoGateway implements PartnerGateway {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "cnpj", "createdAt", "updatedAt");

    private final MongoTemplate mongoTemplate;

    public PartnerMongoGateway(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
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
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(id.getValue())),
                PartnerDocument.class, PartnerDocument.COLLECTION);
    }

    @Override
    public Optional<Partner> findById(final PartnerID id) {
        return Optional
                .ofNullable(mongoTemplate.findById(id.getValue(), PartnerDocument.class,
                        PartnerDocument.COLLECTION))
                .map(PartnerDocument::toDomain);
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
        throw e;
    }
}
