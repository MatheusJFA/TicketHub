package com.tickethub.infrastructure.spot;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;
import com.tickethub.infrastructure.spot.persistence.SpotRepository;
import com.tickethub.infrastructure.section.persistence.SectionRepository;
import com.tickethub.infrastructure.shared.persistence.MongoGatewaySupport;

@Component
public class SpotMongoGateway implements SpotGateway {

    private static final Set<String> SORTABLE_FIELDS = Set.of("location", "createdAt", "updatedAt");

    private final MongoTemplate mongoTemplate;
    private final SpotRepository repository;
    private final SectionRepository sections;

    public SpotMongoGateway(final MongoTemplate mongoTemplate, final SpotRepository repository,
            final SectionRepository sections) {
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
        this.repository = Objects.requireNonNull(repository, "'repository' should not be null");
        this.sections = Objects.requireNonNull(sections, "'sections' should not be null");
    }

    @Override
    public Spot create(final Spot spot, final SectionID sectionId) {
        Objects.requireNonNull(sectionId, "'sectionId' should not be null");
        // Resolve the remaining denormalized links from the parent section.
        final var parent = sections.findById(sectionId.getValue()).orElse(null);
        final var document = parent == null
                ? SpotDocument.from(spot, null, sectionId.getValue(), null)
                : SpotDocument.from(spot, parent.showId(), sectionId.getValue(), parent.partnerId());
        return mongoTemplate.insert(document, SpotDocument.COLLECTION).toDomain();
    }

    @Override
    public void deleteById(final SpotID id) {
        repository.deleteById(id.getValue());
    }

    @Override
    public Optional<Spot> findById(final SpotID id) {
        return repository.findById(id.getValue()).map(SpotDocument::toDomain);
    }

    @Override
    public Spot update(final Spot spot) {
        // Preserve the denormalized ownership links (see SectionMongoGateway.update).
        final var existing = repository.findById(spot.getId().getValue()).orElse(null);
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
        return repository.findAllById(ids.stream().map(SpotID::getValue).toList()).stream()
                .map(SpotDocument::id)
                .map(SpotID::from)
                .toList();
    }
}
