package com.tickethub.infrastructure.persistence;

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
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;

@Component
public class SectionMongoGateway implements SectionGateway {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "createdAt", "updatedAt");

    private final MongoTemplate mongoTemplate;

    public SectionMongoGateway(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
    }

    @Override
    public Section create(final Section section) {
        final var unitOfWork = new MongoUnitOfWork(mongoTemplate);
        unitOfWork.registerNew(SectionDocument.COLLECTION, SectionDocument.from(section));
        for (final Spot spot : section.getSpots()) {
            unitOfWork.registerNew(SpotDocument.COLLECTION, SpotDocument.from(spot));
        }
        unitOfWork.commit();
        return section;
    }

    @Override
    public void deleteById(final SectionID id) {
        final var spotIds = Optional
                .ofNullable(mongoTemplate.findById(id.getValue(), SectionDocument.class,
                        SectionDocument.COLLECTION))
                .map(SectionDocument::spotIds)
                .orElseGet(List::of);
        final var unitOfWork = new MongoUnitOfWork(mongoTemplate);
        unitOfWork.registerRemoved(SectionDocument.COLLECTION, id.getValue());
        unitOfWork.registerRemoved(SpotDocument.COLLECTION, spotIds);
        unitOfWork.commit();
    }

    @Override
    public Optional<Section> findById(final SectionID id) {
        return Optional
                .ofNullable(mongoTemplate.findById(id.getValue(), SectionDocument.class,
                        SectionDocument.COLLECTION))
                .map(this::toDomain);
    }

    @Override
    public Section update(final Section section) {
        final var unitOfWork = new MongoUnitOfWork(mongoTemplate);
        final var document = SectionDocument.from(section);
        unitOfWork.registerDirty(SectionDocument.COLLECTION, document.id(), document);
        for (final Spot spot : section.getSpots()) {
            final var spotDocument = SpotDocument.from(spot);
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

    private Section toDomain(final SectionDocument document) {
        final var spotsById = MongoGatewaySupport.spotsByIds(mongoTemplate, document.spotIds()).stream()
                .collect(Collectors.toMap(SpotDocument::id, Function.identity()));
        final Set<Spot> spots = new HashSet<>();
        for (final String spotId : document.spotIds()) {
            final var spot = spotsById.get(spotId);
            if (spot != null) {
                spots.add(spot.toDomain());
            }
        }
        return document.toDomain(spots);
    }
}
