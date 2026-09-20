package com.tickethub.infrastructure.api.controllers;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import org.springframework.web.bind.annotation.RestController;

import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.infrastructure.api.AuditAPI;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.audit.AuditLogQuery;
import com.tickethub.infrastructure.audit.AuditLogReader;
import com.tickethub.infrastructure.audit.AuditLogResponse;
import com.tickethub.infrastructure.audit.AuditOutcome;
import com.tickethub.infrastructure.audit.MongoAuditLogReader;
import org.apache.commons.lang3.StringUtils;

@RestController
public class AuditController implements AuditAPI {

    private final AuditLogReader reader;

    public AuditController(final AuditLogReader reader) {
        this.reader = reader;
    }

    @Override
    public Pagination<AuditLogResponse> list(final String action, final String actor,
            final String outcome, final String correlationId, final String from, final String to,
            final String search, final int page, final int perPage, final String sort,
            final String direction) {
        final var searchQuery = HttpResults.search(search, page, perPage, sort, direction);
        if (!MongoAuditLogReader.SORTABLE_FIELDS.contains(searchQuery.sort())) {
            throw new DomainException("Invalid sort field: expected one of "
                    + String.join(", ", MongoAuditLogReader.SORTABLE_FIELDS));
        }
        return reader.search(new AuditLogQuery(
                Optional.ofNullable(action).filter(StringUtils::isNotBlank),
                Optional.ofNullable(actor).filter(StringUtils::isNotBlank),
                parseOutcome(outcome),
                Optional.ofNullable(correlationId).filter(StringUtils::isNotBlank),
                parseInstant(from, "from"),
                parseInstant(to, "to"),
                searchQuery.searchTerm(),
                searchQuery.page(),
                searchQuery.perPage(),
                searchQuery.sort(),
                searchQuery.direction()));
    }

    private static Optional<AuditOutcome> parseOutcome(final String outcome) {
        return Optional.ofNullable(outcome)
                .filter(StringUtils::isNotBlank)
                .map(value -> {
                    try {
                        return AuditOutcome.valueOf(value.toUpperCase(Locale.ROOT));
                    } catch (final IllegalArgumentException e) {
                        throw new DomainException("Invalid outcome: expected one of "
                                + String.join(", ", outcomeNames()));
                    }
                });
    }

    private static String[] outcomeNames() {
        return Arrays.stream(AuditOutcome.values()).map(Enum::name).toArray(String[]::new);
    }

    private static Optional<Instant> parseInstant(final String value, final String parameter) {
        return Optional.ofNullable(value)
                .filter(StringUtils::isNotBlank)
                .map(content -> {
                    try {
                        return Instant.parse(content);
                    } catch (final DateTimeParseException e) {
                        throw new DomainException(
                                "Invalid " + parameter + ": expected ISO-8601 instant, got '" + content + "'");
                    }
                });
    }
}
