package com.tickethub.infrastructure.spot;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;
import com.tickethub.infrastructure.shared.persistence.MongoGatewaySupport;

@Component
public class SpotMongoGateway implements SpotGateway {

    private static final Set<String> SORTABLE_FIELDS = Set.of("location", "createdAt", "updatedAt");

    private final MongoTemplate mongoTemplate;

    public SpotMongoGateway(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
    }

    @Override
    public Spot create(final Spot spot) {
        return mongoTemplate.insert(SpotDocument.from(spot), SpotDocument.COLLECTION).toDomain();
    }

    @Override
    public void deleteById(final SpotID id) {
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(id.getValue())),
                SpotDocument.class, SpotDocument.COLLECTION);
    }

    @Override
    public Optional<Spot> findById(final SpotID id) {
        return Optional
                .ofNullable(mongoTemplate.findById(id.getValue(), SpotDocument.class, SpotDocument.COLLECTION))
                .map(SpotDocument::toDomain);
    }

    @Override
    public Spot update(final Spot spot) {
        // Preserve the denormalized ownership links (see SectionMongoGateway.update).
        final var existing = mongoTemplate.findById(spot.getId().getValue(), SpotDocument.class,
                SpotDocument.COLLECTION);
        final var document = existing == null
                ? SpotDocument.from(spot)
                : SpotDocument.from(spot, existing.showId(), existing.sectionId(), existing.partnerId());
        return mongoTemplate.save(document, SpotDocument.COLLECTION).toDomain();
    }

    @Override
    public Pagination<Spot> findAll(final SearchQuery query) {
        final var mongoQuery = MongoGatewaySupport.searchQuery(query, "location");
        return MongoGatewaySupport.paginate(mongoTemplate, mongoQuery, SpotDocument.class,
                SpotDocument.COLLECTION, query, SORTABLE_FIELDS, SpotDocument::toDomain);
    }
}
