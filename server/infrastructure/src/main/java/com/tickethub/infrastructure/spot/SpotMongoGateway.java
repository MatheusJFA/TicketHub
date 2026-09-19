package com.tickethub.infrastructure.spot;

import java.util.List;
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
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;
import com.tickethub.infrastructure.section.persistence.SectionDocument;
import com.tickethub.infrastructure.shared.persistence.MongoGatewaySupport;

@Component
public class SpotMongoGateway implements SpotGateway {

    private static final Set<String> SORTABLE_FIELDS = Set.of("location", "createdAt", "updatedAt");

    private final MongoTemplate mongoTemplate;

    public SpotMongoGateway(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
    }

    @Override
    public Spot create(final Spot spot, final SectionID sectionId) {
        Objects.requireNonNull(sectionId, "'sectionId' should not be null");
        // Resolve the remaining denormalized links from the parent section.
        final var parent = mongoTemplate.findById(sectionId.getValue(), SectionDocument.class,
                SectionDocument.COLLECTION);
        final var document = parent == null
                ? SpotDocument.from(spot, null, sectionId.getValue(), null)
                : SpotDocument.from(spot, parent.showId(), sectionId.getValue(), parent.partnerId());
        return mongoTemplate.insert(document, SpotDocument.COLLECTION).toDomain();
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

    @Override
    public List<SpotID> existsByIds(final List<SpotID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return MongoGatewaySupport
                .existingIds(mongoTemplate, ids.stream().map(SpotID::getValue).toList(),
                        SpotDocument.COLLECTION)
                .stream()
                .map(SpotID::from)
                .toList();
    }
}
