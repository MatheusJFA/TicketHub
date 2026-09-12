package com.tickethub.application.show.retrieve.list;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.core.show.ShowGateway;



public final class DefaultListShowsUseCase extends ListShowsUseCase {
    private final ShowGateway showGateway;

    public DefaultListShowsUseCase(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, Pagination<ListShowsOutput>> execute(final SearchQuery input) {
        Objects.requireNonNull(input);
        try {
            final var page = showGateway.findAll(input);
            final var output = page.map(ListShowsOutput::from);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
