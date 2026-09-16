package com.tickethub.application.show.create;

import com.tickethub.domain.core.show.Show;

import com.tickethub.domain.core.partner.Partner;
import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

public final class DefaultCreateShowUseCase extends CreateShowUseCase {
    private final ShowGateway showGateway;
    private final PartnerGateway partnerGateway;

    public DefaultCreateShowUseCase(final ShowGateway showGateway, final PartnerGateway partnerGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
        this.partnerGateway = Objects.requireNonNull(partnerGateway);
    }

    @Override
    public Either<Notification, CreateShowOutput> execute(final CreateShowCommand command) {
        try {
            final PartnerID partnerId = PartnerID.from(command.partnerId());

            final Optional<Partner> entity = partnerGateway.findById(partnerId);

            if (!entity.isPresent()) {
                return Either.left(notFound("Partner", command.partnerId()));
            }

            final Partner partner = entity.get();
            final Show show = partner.createShow(
                    command.name(),
                    command.description(),
                    command.date(),
                    command.address(),
                    command.totalSpots());

            final Notification notification = Notification.create();
            show.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Show savedEntity = showGateway.create(show);
            final CreateShowOutput output = CreateShowOutput.from(savedEntity);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
