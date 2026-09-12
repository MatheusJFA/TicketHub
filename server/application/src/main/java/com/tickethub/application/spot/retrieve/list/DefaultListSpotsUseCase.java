package com.tickethub.application.spot.retrieve.list;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.core.spot.SpotGateway;



public final class DefaultListSpotsUseCase extends ListSpotsUseCase {
    private final SpotGateway spotGateway;

    public DefaultListSpotsUseCase(final SpotGateway spotGateway) {
        this.spotGateway = Objects.requireNonNull(spotGateway);
    }

    @Override
    public Either<Notification, Pagination<ListSpotsOutput>> execute(final SearchQuery input) {
        Objects.requireNonNull(input);
        try {
            final var page = spotGateway.findAll(input);
            final var output = page.map(ListSpotsOutput::from);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
