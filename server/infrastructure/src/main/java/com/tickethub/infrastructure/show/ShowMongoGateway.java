package com.tickethub.infrastructure.show;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.infrastructure.show.persistence.ShowDocument;
import com.tickethub.infrastructure.show.persistence.ShowRepository;
import com.tickethub.infrastructure.section.persistence.SectionDocument;
import com.tickethub.infrastructure.section.persistence.SectionRepository;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;
import com.tickethub.infrastructure.spot.persistence.SpotRepository;
import com.tickethub.infrastructure.shared.persistence.MongoGatewaySupport;
import com.tickethub.infrastructure.shared.persistence.MongoUnitOfWork;

@Component
public class ShowMongoGateway implements ShowGateway {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "createdAt", "updatedAt");

    private final MongoTemplate mongoTemplate;
    private final ShowRepository repository;
    private final SectionRepository sections;
    private final SpotRepository spots;

    public ShowMongoGateway(final MongoTemplate mongoTemplate, final ShowRepository repository,
            final SectionRepository sections, final SpotRepository spots) {
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
        this.repository = Objects.requireNonNull(repository, "'repository' should not be null");
        this.sections = Objects.requireNonNull(sections, "'sections' should not be null");
        this.spots = Objects.requireNonNull(spots, "'spots' should not be null");
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
        final var sectionIds = repository.findById(id.getValue())
                .map(ShowDocument::sectionIds)
                .orElseGet(List::of);
        final var spotIds = sections.findAllById(sectionIds).stream()
                .filter(section -> nonNull(section.spotIds()))
                .flatMap(section -> section.spotIds().stream())
                .toList();
        unitOfWork.registerRemoved(SpotDocument.COLLECTION, spotIds);
        unitOfWork.registerRemoved(SectionDocument.COLLECTION, sectionIds);
        unitOfWork.registerRemoved(ShowDocument.COLLECTION, id.getValue());
        unitOfWork.commit();
        // Children written with denormalized showId (newer docs) are removed by parent field,
        // covering sections/spots added outside the legacy id arrays.
        MongoGatewaySupport.removeSpotsByShowId(mongoTemplate, id.getValue());
        MongoGatewaySupport.removeSectionsByShowId(mongoTemplate, id.getValue());
    }

    @Override
    public Optional<Show> findById(final ShowID id) {
        return repository.findById(id.getValue()).map(this::toDomain);
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

    @Override
    public List<ShowID> existsByIds(final List<ShowID> ids) {
        if (isEmpty(ids)) {
            return List.of();
        }
        return repository.findAllById(ids.stream().map(ShowID::getValue).toList()).stream()
                .map(ShowDocument::id)
                .map(ShowID::from)
                .toList();
    }

    @Override
    public void appendSpots(final ShowID showId, final SectionID sectionId, final Set<Spot> spots) {
        if (isEmpty(spots)) {
            return;
        }
        // Skip orphan writes when the parent section no longer exists.
        if (!sections.existsById(sectionId.getValue())) {
            return;
        }
        final String partnerId = repository.findById(showId.getValue())
                .map(ShowDocument::partnerId)
                .orElse(null);
        final var unitOfWork = new MongoUnitOfWork(mongoTemplate);
        for (final Spot spot : spots) {
            unitOfWork.registerNew(SpotDocument.COLLECTION,
                    SpotDocument.from(spot, showId.getValue(), sectionId.getValue(), partnerId));
        }
        unitOfWork.commit();
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(sectionId.getValue())),
                new Update().push("spotIds").each(
                        spots.stream().map(spot -> spot.getId().getValue()).toArray()),
                SectionDocument.class, SectionDocument.COLLECTION);
    }

    private void registerGraph(final MongoUnitOfWork unitOfWork, final Show show, final boolean dirty) {
        final String showId = show.getId().getValue();
        final String partnerId = Optional.ofNullable(show.getPartnerId())
                .map(PartnerID::getValue).orElse(null);
        for (final Section section : show.getSections()) {
            final var sectionDocument = SectionDocument.from(section, showId, partnerId);
            if (dirty) {
                unitOfWork.registerDirty(SectionDocument.COLLECTION, sectionDocument.id(), sectionDocument);
            } else {
                unitOfWork.registerNew(SectionDocument.COLLECTION, sectionDocument);
            }
            for (final Spot spot : section.getSpots()) {
                final var spotDocument = SpotDocument.from(spot, showId, section.getId().getValue(), partnerId);
                if (dirty) {
                    unitOfWork.registerDirty(SpotDocument.COLLECTION, spotDocument.id(), spotDocument);
                } else {
                    unitOfWork.registerNew(SpotDocument.COLLECTION, spotDocument);
                }
            }
        }
    }

    private Show toDomain(final ShowDocument document) {
        // Prefer the denormalized parent fields (3 reads total, all indexed);
        // fall back to the legacy id arrays for documents written before them.
        List<SectionDocument> sectionDocuments = sections.findByShowId(document.id());
        if (sectionDocuments.isEmpty() && !isEmpty(document.sectionIds())) {
            sectionDocuments = sections.findAllById(document.sectionIds());
        }
        final var spotsBySection = spots.findByShowId(document.id()).stream()
                .filter(spot -> nonNull(spot.sectionId()))
                .collect(Collectors.groupingBy(SpotDocument::sectionId));
        final var sectionsById = sectionDocuments.stream()
                .collect(Collectors.toMap(SectionDocument::id, Function.identity(), (first, second) -> first));
        final Set<Section> sections = new HashSet<>();
        final List<String> order = isEmpty(document.sectionIds())
                ? sectionDocuments.stream().map(SectionDocument::id).toList()
                : document.sectionIds();
        for (final String sectionId : order) {
            final var section = sectionsById.get(sectionId);
            if (nonNull(section)) {
                sections.add(toDomain(section, spotsBySection.getOrDefault(sectionId, List.of())));
            }
        }
        return document.toDomain(sections);
    }

    private Section toDomain(final SectionDocument document, final List<SpotDocument> candidates) {
        final List<SpotDocument> spotDocuments;
        if (!candidates.isEmpty()) {
            spotDocuments = candidates;
        } else if (!isEmpty(document.spotIds())) {
            final var byId = spots.findAllById(document.spotIds()).stream()
                    .collect(Collectors.toMap(SpotDocument::id, Function.identity(), (first, second) -> first));
            spotDocuments = document.spotIds().stream()
                    .map(byId::get)
                    .filter(Objects::nonNull)
                    .toList();
        } else {
            spotDocuments = spots.findBySectionId(document.id());
        }
        final Set<Spot> spots = new HashSet<>();
        for (final SpotDocument spot : spotDocuments) {
            spots.add(spot.toDomain());
        }
        return document.toDomain(spots);
    }
}
