package com.tickethub.infrastructure.persistence;

import java.util.Arrays;
import java.util.List;
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

final class MongoGatewaySupport {

    private MongoGatewaySupport() {
    }

    static Query searchQuery(final SearchQuery query, final String... fields) {
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

    static Sort sortOf(final SearchQuery query, final Set<String> allowedFields, final String fallback) {
        final String sort = query.sort() != null && allowedFields.contains(query.sort())
                ? query.sort()
                : fallback;
        final Sort.Direction direction = "desc".equalsIgnoreCase(query.direction())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, sort);
    }

    static <D, T> Pagination<T> paginate(final MongoTemplate mongoTemplate, final Query query,
            final Class<D> documentType, final String collection, final SearchQuery search,
            final Set<String> sortableFields, final Function<D, T> mapper) {
        final long total = mongoTemplate.count(query, documentType, collection);
        final var page = mongoTemplate.find(
                query.with(PageRequest.of(search.page(), search.perPage(),
                        sortOf(search, sortableFields, "createdAt"))),
                documentType, collection);
        return new Pagination<>(search.page(), search.perPage(), total, page.stream().map(mapper).toList());
    }

    static List<SectionDocument> sectionsByIds(final MongoTemplate mongoTemplate, final List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mongoTemplate.find(Query.query(Criteria.where("_id").in(ids)),
                SectionDocument.class, SectionDocument.COLLECTION);
    }

    static List<SpotDocument> spotsByIds(final MongoTemplate mongoTemplate, final List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mongoTemplate.find(Query.query(Criteria.where("_id").in(ids)),
                SpotDocument.class, SpotDocument.COLLECTION);
    }
}
