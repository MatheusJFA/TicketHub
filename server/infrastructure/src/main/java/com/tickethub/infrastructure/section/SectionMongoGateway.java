package com.tickethub.infrastructure.section;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.infrastructure.section.persistence.SectionDocument;
import com.tickethub.infrastructure.section.persistence.SectionRepository;
import com.tickethub.infrastructure.show.persistence.ShowDocument;
import com.tickethub.infrastructure.show.persistence.ShowRepository;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;
import com.tickethub.infrastructure.spot.persistence.SpotRepository;
import com.tickethub.infrastructure.shared.persistence.MongoGatewaySupport;
import com.tickethub.infrastructure.shared.persistence.MongoUnitOfWork;

@Component
public class SectionMongoGateway implements SectionGateway {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "createdAt", "updatedAt");

    private final MongoTemplate mongoTemplate;
    private final SectionRepository repository;
    private final ShowRepository shows;
    private final SpotRepository spots;

    public SectionMongoGateway(final MongoTemplate mongoTemplate, final SectionRepository repository,
            final ShowRepository shows, final SpotRepository spots) {
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
        this.repository = Objects.requireNonNull(repository, "'repository' should not be null");
        this.shows = Objects.requireNonNull(shows, "'shows' should not be null");
        this.spots = Objects.requireNonNull(spots, "'spots' should not be null");
    }

    @Override
    public Section create(final Section section, final ShowID showId) {
        Objects.requireNonNull(showId, "'showId' should not be null");
        // Resolve the remaining denormalized links from the parent show.
        final var partnerId = shows.findById(showId.getValue())
                .map(ShowDocument::partnerId)
                .orElse(null);
        final var unitOfWork = new MongoUnitOfWork(mongoTemplate);
        unitOfWork.registerNew(SectionDocument.COLLECTION,
                SectionDocument.from(section, showId.getValue(), partnerId));
        for (final Spot spot : section.getSpots()) {
            unitOfWork.registerNew(SpotDocument.COLLECTION,
                    SpotDocument.from(spot, showId.getValue(), section.getId().getValue(), partnerId));
        }
        unitOfWork.commit();
        return section;
    }

    @Override
    public void deleteById(final SectionID id) {
        final var spotIds = repository.findById(id.getValue())
                .map(SectionDocument::spotIds)
                .orElseGet(List::of);
        final var unitOfWork = new MongoUnitOfWork(mongoTemplate);
        unitOfWork.registerRemoved(SectionDocument.COLLECTION, id.getValue());
        unitOfWork.registerRemoved(SpotDocument.COLLECTION, spotIds);
        unitOfWork.commit();
        // Spots written with denormalized sectionId (via the Show graph) are removed by parent field.
        MongoGatewaySupport.removeSpotsBySectionId(mongoTemplate, id.getValue());
    }

    @Override
    public Optional<Section> findById(final SectionID id) {
        return repository.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    public Section update(final Section section) {
        // Preserve the denormalized ownership links: standalone updates carry no parent context,
        // so carry over whatever the Show graph stored instead of wiping it with nulls.
        final var links = repository.findById(section.getId().getValue())
                .map(existing -> new String[] { existing.showId(), existing.partnerId() })
                .orElseGet(() -> new String[] { null, null });
        final var unitOfWork = new MongoUnitOfWork(mongoTemplate);
        final var document = SectionDocument.from(section, links[0], links[1]);
        unitOfWork.registerDirty(SectionDocument.COLLECTION, document.id(), document);
        final var existingSpots = spots.findBySectionId(document.id()).stream()
                .collect(Collectors.toMap(SpotDocument::id, Function.identity(), (first, second) -> first));
        for (final Spot spot : section.getSpots()) {
            final var existing = existingSpots.get(spot.getId().getValue());
            final var spotDocument = existing == null
                    ? SpotDocument.from(spot)
                    : SpotDocument.from(spot, existing.showId(), existing.sectionId(), existing.partnerId());
            unitOfWork.registerDirty(SpotDocument.COLLECTION, spotDocument.id(), spotDocument);
        }
        unitOfWork.commit();
        return section;
    }

    @Override
    public Pagination<Section> findAll(final SearchQuery query) {
        final var mongoQuery = MongoGatewaySupport.searchQuery(query, "name", "description");
        return MongoGatewaySupport.paginate(mongoTemplate, mongoQuery, SectionDocument.class,
                SectionDocument.COLLECTION, query, SORTABLE_FIELDS, this::toDomain);
    }

    @Override
    public List<SectionID> existsByIds(final List<SectionID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return repository.findAllById(ids.stream().map(SectionID::getValue).toList()).stream()
                .map(SectionDocument::id)
                .map(SectionID::from)
                .toList();
    }

    private Section toDomain(final SectionDocument document) {
        // Prefer the denormalized parent field (single indexed query);
        // fall back to the legacy id array for older documents.
        List<SpotDocument> spotDocuments = spots.findBySectionId(document.id());
        if (spotDocuments.isEmpty() && document.spotIds() != null && !document.spotIds().isEmpty()) {
            final var spotsById = spots.findAllById(document.spotIds()).stream()
                    .collect(Collectors.toMap(SpotDocument::id, Function.identity(),
                            (first, second) -> first));
            spotDocuments = document.spotIds().stream()
                    .map(spotsById::get)
                    .filter(Objects::nonNull)
                    .toList();
        }
        final Set<Spot> spots = new HashSet<>();
        for (final SpotDocument spot : spotDocuments) {
            spots.add(spot.toDomain());
        }
        return document.toDomain(spots);
    }
}
