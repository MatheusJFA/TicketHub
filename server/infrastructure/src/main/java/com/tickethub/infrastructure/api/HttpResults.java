package com.tickethub.infrastructure.api;

import java.util.Locale;
import java.util.Optional;

import org.springframework.web.server.ResponseStatusException;

import com.tickethub.application.Either;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.validation.Notification;

/**
 * Pure translation between use case results and the HTTP layer: unwraps
 * successful results, maps failures to exceptions handled by the
 * {@code GlobalExceptionHandler}, and validates listing parameters.
 *
 * <p>Cross-cutting behavior (retry, circuit breaking, auditing) lives in the
 * {@code UseCaseMonitoringAspect}, not here.
 */
public final class HttpResults {

    private HttpResults() {
    }

    public static <O> O require(final Either<Notification, O> result) {
        if (result.isRight()) {
            return result.getRight();
        }
        throw toException(result.getLeft());
    }

    public static void requireEmpty(final Optional<Notification> failure) {
        if (failure.isEmpty()) {
            return;
        }
        throw toException(failure.get());
    }

    public static RuntimeException toException(final Notification notification) {
        if (notification.getCause() instanceof ResponseStatusException status) {
            return status;
        }
        if (notification.getCause() != null && !(notification.getCause() instanceof DomainException)) {
            return new IllegalStateException("Use case failed", notification.getCause());
        }
        return new ApiValidationException(notification);
    }

    public static SearchQuery search(String search, int page, int perPage, String sort, String direction) {
        if (page < 0 || perPage < 1 || perPage > 100 || sort == null || sort.isBlank()
                || direction == null
                || !(direction.equalsIgnoreCase("asc") || direction.equalsIgnoreCase("desc"))) {
            throw new DomainException("Invalid pagination parameters: page >= 0, perPage between 1 and 100, sort non-blank, dir asc or desc");
        }
        return new SearchQuery(page, perPage, search, sort, direction.toLowerCase(Locale.ROOT));
    }
}
