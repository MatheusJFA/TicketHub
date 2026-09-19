package com.tickethub.infrastructure.shared.persistence;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;
import com.tickethub.infrastructure.section.persistence.SectionDocument;

public final class MongoGatewaySupport {

    private MongoGatewaySupport() {
    }

    public static Query searchQuery(final SearchQuery query, final String... fields) {
        final Query mongoQuery = new Query();
        if (query.searchTerm() != null && !query.searchTerm().isBlank() && fields.length > 0) {
            final var pattern = Pattern.compile(Pattern.quote(query.searchTerm().trim()),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            mongoQuery.addCriteria(new Criteria().orOperator(
                    Arrays.stream(fields)
                            .map(field -> Criteria.where(field).regex(pattern))
                            .toList()));
        }
        return mongoQuery;
    }

    public static Sort sortOf(final SearchQuery query, final Set<String> allowedFields, final String fallback) {
        final String sort = query.sort() != null && allowedFields.contains(query.sort())
                ? query.sort()
                : fallback;
        final Sort.Direction direction = "desc".equalsIgnoreCase(query.direction())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, sort);
    }

    public static <D, T> Pagination<T> paginate(final MongoTemplate mongoTemplate, final Query query,
            final Class<D> documentType, final String collection, final SearchQuery search,
            final Set<String> sortableFields, final Function<D, T> mapper) {
        final long total = mongoTemplate.count(query, documentType, collection);
        final var page = mongoTemplate.find(
                query.with(PageRequest.of(search.page(), search.perPage(),
                        sortOf(search, sortableFields, "createdAt"))),
                documentType, collection);
        return new Pagination<>(search.page(), search.perPage(), total, page.stream().map(mapper).toList());
    }

    public static List<SectionDocument> sectionsByIds(final MongoTemplate mongoTemplate, final List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mongoTemplate.find(Query.query(Criteria.where("_id").in(ids)),
                SectionDocument.class, SectionDocument.COLLECTION);
    }

    public static List<SectionDocument> sectionsByShowId(final MongoTemplate mongoTemplate, final String showId) {
        if (showId == null || showId.isBlank()) {
            return List.of();
        }
        return mongoTemplate.find(Query.query(Criteria.where("showId").is(showId)),
                SectionDocument.class, SectionDocument.COLLECTION);
    }

    public static List<SpotDocument> spotsByIds(final MongoTemplate mongoTemplate, final List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mongoTemplate.find(Query.query(Criteria.where("_id").in(ids)),
                SpotDocument.class, SpotDocument.COLLECTION);
    }

    public static List<SpotDocument> spotsByShowId(final MongoTemplate mongoTemplate, final String showId) {
        if (showId == null || showId.isBlank()) {
            return List.of();
        }
        return mongoTemplate.find(Query.query(Criteria.where("showId").is(showId)),
                SpotDocument.class, SpotDocument.COLLECTION);
    }

    public static List<SpotDocument> spotsBySectionId(final MongoTemplate mongoTemplate, final String sectionId) {
        if (sectionId == null || sectionId.isBlank()) {
            return List.of();
        }
        return mongoTemplate.find(Query.query(Criteria.where("sectionId").is(sectionId)),
                SpotDocument.class, SpotDocument.COLLECTION);
    }

    public static void removeSectionsByShowId(final MongoTemplate mongoTemplate, final String showId) {
        if (showId == null || showId.isBlank()) {
            return;
        }
        mongoTemplate.remove(Query.query(Criteria.where("showId").is(showId)),
                SectionDocument.class, SectionDocument.COLLECTION);
    }

    public static void removeSpotsByShowId(final MongoTemplate mongoTemplate, final String showId) {
        if (showId == null || showId.isBlank()) {
            return;
        }
        mongoTemplate.remove(Query.query(Criteria.where("showId").is(showId)),
                SpotDocument.class, SpotDocument.COLLECTION);
    }

    public static void removeSpotsBySectionId(final MongoTemplate mongoTemplate, final String sectionId) {
        if (sectionId == null || sectionId.isBlank()) {
            return;
        }
        mongoTemplate.remove(Query.query(Criteria.where("sectionId").is(sectionId)),
                SpotDocument.class, SpotDocument.COLLECTION);
    }

    public static List<String> existingIds(final MongoTemplate mongoTemplate, final List<String> ids,
            final String collection) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        final var query = Query.query(Criteria.where("_id").in(ids));
        query.fields().include("_id");
        return mongoTemplate.find(query, org.bson.Document.class, collection).stream()
                .map(document -> document.getString("_id"))
                .filter(Objects::nonNull)
                .toList();
    }
}
