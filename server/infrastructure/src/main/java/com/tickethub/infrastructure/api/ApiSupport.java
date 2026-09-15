package com.tickethub.infrastructure.api;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.validation.Notification;

public final class ApiSupport {
    private ApiSupport() {}

    public static <I, O> O execute(UseCase<I, Either<Notification, O>> useCase, I input) {
        return useCase.execute(input).fold(notification -> {
            if (notification.getCause() != null && !(notification.getCause() instanceof DomainException)) {
                throw new IllegalStateException("Use case failed", notification.getCause());
            }
            throw new ApiValidationException(notification);
        }, output -> output);
    }

    public static SearchQuery query(String search, int page, int perPage, String sort, String direction) {
        if (page < 0 || perPage < 1 || perPage > 100 || sort.isBlank()
                || !(direction.equalsIgnoreCase("asc") || direction.equalsIgnoreCase("desc"))) {
            throw new DomainException("Invalid pagination parameters: page >= 0, perPage between 1 and 100, dir asc or desc");
        }
        return new SearchQuery(page, perPage, search, sort, direction.toLowerCase(java.util.Locale.ROOT));
    }
}
