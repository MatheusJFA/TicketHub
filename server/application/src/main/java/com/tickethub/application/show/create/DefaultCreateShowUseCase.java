package com.tickethub.application.show.create;

import java.util.Objects;

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
        Objects.requireNonNull(command);
        try {
            final var entity = partnerGateway.findById(PartnerID.from(command.partnerId()));

            if (!entity.isPresent()) {
                return Either.left(Notification.create(new Error("Partner not found: " + command.partnerId())));
            }

            final var partner = entity.get();
            final var show = partner.createShow(
                    command.name(),
                    command.description(),
                    command.date(),
                    command.address(),
                    command.totalSpots());

            final var notification = Notification.create();
            show.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            return Either.right(CreateShowOutput.from(showGateway.create(show)));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
