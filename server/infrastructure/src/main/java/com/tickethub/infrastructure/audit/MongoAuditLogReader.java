package com.tickethub.infrastructure.audit;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.bson.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.tickethub.domain.pagination.Pagination;

@Component
public class MongoAuditLogReader implements AuditLogReader {

    public static final Set<String> SORTABLE_FIELDS = Set.of(
            "occurredAt", "action", "actor", "outcome", "correlationId", "durationMs");

    private final MongoTemplate mongoTemplate;

    public MongoAuditLogReader(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
    }

    @Override
    public Pagination<AuditLogResponse> search(final AuditLogQuery query) {
        final var mongoQuery = new Query();
        query.action().ifPresent(action -> mongoQuery.addCriteria(Criteria.where("action").is(action)));
        query.actor().ifPresent(actor -> mongoQuery.addCriteria(Criteria.where("actor").is(actor)));
        query.outcome().ifPresent(outcome -> mongoQuery.addCriteria(Criteria.where("outcome").is(outcome.name())));
        query.correlationId()
                .ifPresent(correlationId -> mongoQuery.addCriteria(Criteria.where("correlationId").is(correlationId)));
        if (query.from().isPresent() || query.to().isPresent()) {
            final var occurredAt = Criteria.where("occurredAt");
            query.from().ifPresent(occurredAt::gte);
            query.to().ifPresent(occurredAt::lte);
            mongoQuery.addCriteria(occurredAt);
        }
        if (isNotBlank(query.searchTerm())) {
            final var pattern = Pattern.compile(Pattern.quote(query.searchTerm().trim()),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            mongoQuery.addCriteria(new Criteria().orOperator(
                    Arrays.stream(new String[] { "action", "actor", "correlationId" })
                            .map(field -> Criteria.where(field).regex(pattern))
                            .toList()));
        }

        final long total = mongoTemplate.count(mongoQuery, Document.class, MongoAuditTrail.COLLECTION);
        final var direction = "desc".equalsIgnoreCase(query.direction()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        final var page = mongoTemplate.find(
                mongoQuery.with(PageRequest.of(query.page(), query.perPage(),
                        Sort.by(direction, query.sort()))),
                Document.class, MongoAuditTrail.COLLECTION);
        return new Pagination<>(query.page(), query.perPage(), total,
                page.stream().map(MongoAuditLogReader::toResponse).toList());
    }

    static AuditLogResponse toResponse(final Document document) {
        return new AuditLogResponse(
                Optional.ofNullable(document.getObjectId("_id")).map(Object::toString).orElse(null),
                Optional.ofNullable(document.getDate("occurredAt")).map(occurred -> occurred.toInstant()).orElse(null),
                document.getString("correlationId"),
                document.getString("actor"),
                document.getString("action"),
                document.getString("input"),
                Optional.ofNullable(document.getString("outcome")).map(AuditOutcome::valueOf).orElse(null),
                document.getString("error"),
                Optional.ofNullable(document.get("durationMs"))
                        .map(value -> ((Number) value).longValue())
                        .orElse(0L));
    }
}
