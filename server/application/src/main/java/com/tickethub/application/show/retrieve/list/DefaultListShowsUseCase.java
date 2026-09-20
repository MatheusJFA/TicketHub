package com.tickethub.application.show.retrieve.list;

import static java.util.Objects.requireNonNull;
import com.tickethub.domain.core.show.Show;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.core.show.ShowGateway;



public class DefaultListShowsUseCase extends ListShowsUseCase {
    private final ShowGateway showGateway;

    public DefaultListShowsUseCase(final ShowGateway showGateway) {
        this.showGateway = requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, Pagination<ListShowsOutput>> execute(final SearchQuery input) {
        try {
            final Pagination<Show> page = showGateway.findAll(input);
            final Pagination<ListShowsOutput> output = page.map(ListShowsOutput::from);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
