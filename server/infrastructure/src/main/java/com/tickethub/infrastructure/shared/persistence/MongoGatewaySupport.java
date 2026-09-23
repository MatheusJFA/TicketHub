package com.tickethub.infrastructure.shared.persistence;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.infrastructure.section.persistence.SectionDocument;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public final class MongoGatewaySupport {

    private MongoGatewaySupport() {}

    public static Query searchQuery(final SearchQuery query, final String... fields) {
        final Query mongoQuery = new Query();
        if (isNotBlank(query.searchTerm()) && fields.length > 0) {
            // Filtra: termo de busca literal (case-insensitive) nos campos de texto do MongoDB.
            final var pattern = Pattern.compile(
                    Pattern.quote(query.searchTerm().trim()), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            mongoQuery.addCriteria(new Criteria()
                    .orOperator(Arrays.stream(fields)
                            .map(field -> Criteria.where(field).regex(pattern))
                            .toList()));
        }
        return mongoQuery;
    }

    public static Sort sortOf(final SearchQuery query, final Set<String> allowedFields, final String fallback) {
        final String sort = nonNull(query.sort()) && allowedFields.contains(query.sort()) ? query.sort() : fallback;
        final Sort.Direction direction =
                "desc".equalsIgnoreCase(query.direction()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sort);
    }

    public static <D, T> Pagination<T> paginate(
            final MongoTemplate mongoTemplate,
            final Query query,
            final Class<D> documentType,
            final String collection,
            final SearchQuery search,
            final Set<String> sortableFields,
            final Function<D, T> mapper) {
        final long total = mongoTemplate.count(query, documentType, collection);
        final var page = mongoTemplate.find(
                query.with(
                        PageRequest.of(search.page(), search.perPage(), sortOf(search, sortableFields, "createdAt"))),
                documentType,
                collection);
        return new Pagination<>(
                search.page(),
                search.perPage(),
                total,
                page.stream().map(mapper).toList());
    }

    public static void removeSectionsByShowId(final MongoTemplate mongoTemplate, final String showId) {
        if (isBlank(showId)) {
            return;
        }
        mongoTemplate.remove(
                Query.query(Criteria.where("showId").is(showId)), SectionDocument.class, SectionDocument.COLLECTION);
    }

    public static void removeSpotsByShowId(final MongoTemplate mongoTemplate, final String showId) {
        if (isBlank(showId)) {
            return;
        }
        mongoTemplate.remove(
                Query.query(Criteria.where("showId").is(showId)), SpotDocument.class, SpotDocument.COLLECTION);
    }

    public static void removeSpotsBySectionId(final MongoTemplate mongoTemplate, final String sectionId) {
        if (isBlank(sectionId)) {
            return;
        }
        mongoTemplate.remove(
                Query.query(Criteria.where("sectionId").is(sectionId)), SpotDocument.class, SpotDocument.COLLECTION);
    }
}
