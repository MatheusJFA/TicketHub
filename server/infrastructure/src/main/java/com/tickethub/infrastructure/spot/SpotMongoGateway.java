package com.tickethub.infrastructure.spot;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.core.spot.SpotPlacement;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.infrastructure.audit.AuditActor;
import com.tickethub.infrastructure.section.persistence.SectionRepository;
import com.tickethub.infrastructure.shared.persistence.MongoGatewaySupport;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;
import com.tickethub.infrastructure.spot.persistence.SpotRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class SpotMongoGateway implements SpotGateway {

    private static final Set<String> SORTABLE_FIELDS = Set.of("location", "createdAt", "updatedAt");

    private final MongoTemplate mongoTemplate;
    private final SpotRepository repository;
    private final SectionRepository sections;

    public SpotMongoGateway(
            final MongoTemplate mongoTemplate, final SpotRepository repository, final SectionRepository sections) {
        this.mongoTemplate = requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
        this.repository = requireNonNull(repository, "'repository' should not be null");
        this.sections = requireNonNull(sections, "'sections' should not be null");
    }

    @Override
    public Spot create(final Spot spot, final SectionID sectionId) {
        requireNonNull(sectionId, "'sectionId' should not be null");
        // Resolve the remaining denormalized links from the parent section.
        final var parent = sections.findById(sectionId.getValue()).orElse(null);
        final var document = Optional.ofNullable(parent)
                .map(current -> SpotDocument.from(spot, current.showId(), sectionId.getValue(), current.partnerId()))
                .orElseGet(() -> SpotDocument.from(spot, null, sectionId.getValue(), null));
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
    public Optional<Spot> reserveIfAvailable(final SpotID id) {
        requireNonNull(id, "'id' should not be null");
        // Single atomic findAndModify: only a free spot flips to reserved, so
        // concurrent buyers cannot hold the same seat. Documents written
        // before the `reserved` field existed are treated as unreserved.
        final var query = new Query(Criteria.where("_id")
                .is(id.getValue())
                .and("available")
                .is(true)
                .andOperator(new Criteria()
                        .orOperator(
                                Criteria.where("reserved").is(false),
                                Criteria.where("reserved").exists(false))));
        final var update = new Update()
                .set("reserved", true)
                .set("updatedAt", Instant.now())
                .set("lastModifiedBy", AuditActor.currentOrAnonymous());
        final var options = FindAndModifyOptions.options().returnNew(true);
        return Optional.ofNullable(mongoTemplate.findAndModify(
                        query, update, options, SpotDocument.class, SpotDocument.COLLECTION))
                .map(SpotDocument::toDomain);
    }

    @Override
    public Optional<SpotPlacement> findPlacement(final SpotID id) {
        requireNonNull(id, "'id' should not be null");
        return repository
                .findById(id.getValue())
                .map(document -> new SpotPlacement(document.toDomain(), document.showId(), document.sectionId()));
    }

    @Override
    public Spot update(final Spot spot) {
        // Preserve the denormalized ownership links (see SectionMongoGateway.update).
        final var existing = repository.findById(spot.getId().getValue()).orElse(null);
        final var document = Optional.ofNullable(existing)
                .map(current -> SpotDocument.from(spot, current.showId(), current.sectionId(), current.partnerId()))
                .orElseGet(() -> SpotDocument.from(spot));
        return mongoTemplate.save(document, SpotDocument.COLLECTION).toDomain();
    }

    @Override
    public Pagination<Spot> findAll(final SearchQuery query) {
        final var mongoQuery = MongoGatewaySupport.searchQuery(query, "location");
        return MongoGatewaySupport.paginate(
                mongoTemplate,
                mongoQuery,
                SpotDocument.class,
                SpotDocument.COLLECTION,
                query,
                SORTABLE_FIELDS,
                SpotDocument::toDomain);
    }

    @Override
    public Pagination<Spot> findBySection(final SectionID sectionId, final SearchQuery query) {
        requireNonNull(sectionId, "'sectionId' should not be null");
        final var mongoQuery = MongoGatewaySupport.searchQuery(query, "location");
        mongoQuery.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("sectionId")
                .is(sectionId.getValue()));
        return MongoGatewaySupport.paginate(
                mongoTemplate,
                mongoQuery,
                SpotDocument.class,
                SpotDocument.COLLECTION,
                query,
                SORTABLE_FIELDS,
                SpotDocument::toDomain);
    }

    @Override
    public List<SpotID> existsByIds(final List<SpotID> ids) {
        if (isEmpty(ids)) {
            return List.of();
        }
        return repository.findAllById(ids.stream().map(SpotID::getValue).toList()).stream()
                .map(SpotDocument::id)
                .map(SpotID::from)
                .toList();
    }
}
