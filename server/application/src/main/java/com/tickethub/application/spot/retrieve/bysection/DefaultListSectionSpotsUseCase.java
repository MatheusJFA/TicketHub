package com.tickethub.application.spot.retrieve.bysection;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.application.spot.retrieve.list.ListSpotsOutput;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.validation.Notification;

public class DefaultListSectionSpotsUseCase extends ListSectionSpotsUseCase {
    private final SpotGateway spotGateway;

    public DefaultListSectionSpotsUseCase(final SpotGateway spotGateway) {
        this.spotGateway = requireNonNull(spotGateway);
    }

    @Override
    public Either<Notification, Pagination<ListSpotsOutput>> execute(final ListSectionSpotsCommand input) {
        try {
            final Pagination<Spot> page = spotGateway.findBySection(SectionID.from(input.sectionId()), input.query());
            return Either.right(page.map(ListSpotsOutput::from));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
