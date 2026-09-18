package com.tickethub.application.spot.retrieve.list;

import com.tickethub.domain.core.spot.Spot;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.core.spot.SpotGateway;



public class DefaultListSpotsUseCase extends ListSpotsUseCase {
    private final SpotGateway spotGateway;

    public DefaultListSpotsUseCase(final SpotGateway spotGateway) {
        this.spotGateway = Objects.requireNonNull(spotGateway);
    }

    @Override
    public Either<Notification, Pagination<ListSpotsOutput>> execute(final SearchQuery input) {
        try {
            final Pagination<Spot> page = spotGateway.findAll(input);
            final Pagination<ListSpotsOutput> output = page.map(ListSpotsOutput::from);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
