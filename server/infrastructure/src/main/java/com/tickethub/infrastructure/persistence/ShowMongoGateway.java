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
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;

@Component
public class ShowMongoGateway implements ShowGateway {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "createdAt", "updatedAt");

    private final MongoTemplate mongoTemplate;

    public ShowMongoGateway(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
    }

    @Override
    public Show create(final Show show) {
        final var unitOfWork = new MongoUnitOfWork(mongoTemplate);
        unitOfWork.registerNew(ShowDocument.COLLECTION, ShowDocument.from(show));
        registerGraph(unitOfWork, show, false);
        unitOfWork.commit();
        return show;
    }

    @Override
    public void deleteById(final ShowID id) {
        final var unitOfWork = new MongoUnitOfWork(mongoTemplate);
        final var sectionIds = Optional
                .ofNullable(mongoTemplate.findById(id.getValue(), ShowDocument.class, ShowDocument.COLLECTION))
                .map(ShowDocument::sectionIds)
                .orElseGet(List::of);
        final var spotIds = MongoGatewaySupport.sectionsByIds(mongoTemplate, sectionIds).stream()
                .flatMap(section -> section.spotIds().stream())
                .toList();
        unitOfWork.registerRemoved(SpotDocument.COLLECTION, spotIds);
        unitOfWork.registerRemoved(SectionDocument.COLLECTION, sectionIds);
        unitOfWork.registerRemoved(ShowDocument.COLLECTION, id.getValue());
        unitOfWork.commit();
    }

    @Override
    public Optional<Show> findById(final ShowID id) {
        return Optional
                .ofNullable(mongoTemplate.findById(id.getValue(), ShowDocument.class, ShowDocument.COLLECTION))
                .map(this::toDomain);
    }

    @Override
    public Show update(final Show show) {
        final var unitOfWork = new MongoUnitOfWork(mongoTemplate);
        final var document = ShowDocument.from(show);
        unitOfWork.registerDirty(ShowDocument.COLLECTION, document.id(), document);
        registerGraph(unitOfWork, show, true);
        unitOfWork.commit();
        return show;
    }

    @Override
    public Pagination<Show> findAll(final SearchQuery query) {
        final var mongoQuery = MongoGatewaySupport.searchQuery(query, "name", "description");
        return MongoGatewaySupport.paginate(mongoTemplate, mongoQuery, ShowDocument.class,
                ShowDocument.COLLECTION, query, SORTABLE_FIELDS, this::toDomain);
    }

    private void registerGraph(final MongoUnitOfWork unitOfWork, final Show show, final boolean dirty) {
        for (final Section section : show.getSections()) {
            final var sectionDocument = SectionDocument.from(section);
            if (dirty) {
                unitOfWork.registerDirty(SectionDocument.COLLECTION, sectionDocument.id(), sectionDocument);
            } else {
                unitOfWork.registerNew(SectionDocument.COLLECTION, sectionDocument);
            }
            for (final Spot spot : section.getSpots()) {
                final var spotDocument = SpotDocument.from(spot);
                if (dirty) {
                    unitOfWork.registerDirty(SpotDocument.COLLECTION, spotDocument.id(), spotDocument);
                } else {
                    unitOfWork.registerNew(SpotDocument.COLLECTION, spotDocument);
                }
            }
        }
    }

    private Show toDomain(final ShowDocument document) {
        final var sectionsById = MongoGatewaySupport.sectionsByIds(mongoTemplate, document.sectionIds()).stream()
                .collect(Collectors.toMap(SectionDocument::id, Function.identity()));
        final Set<Section> sections = new HashSet<>();
        for (final String sectionId : document.sectionIds()) {
            final var section = sectionsById.get(sectionId);
            if (section != null) {
                sections.add(toDomain(section));
            }
        }
        return document.toDomain(sections);
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
