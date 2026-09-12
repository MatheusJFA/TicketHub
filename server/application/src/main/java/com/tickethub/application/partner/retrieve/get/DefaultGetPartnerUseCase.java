package com.tickethub.application.partner.retrieve.get;
import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.validation.Error;

public final class DefaultGetPartnerUseCase extends GetPartnerUseCase {
    private final PartnerGateway partnerGateway;

    public DefaultGetPartnerUseCase(final PartnerGateway partnerGateway) {
        this.partnerGateway = Objects.requireNonNull(partnerGateway);
    }

    @Override
    public Either<Notification, GetPartnerOutput> execute(final String input) {
        Objects.requireNonNull(input);
        try {
            final var entity = partnerGateway.findById(PartnerID.from(input));
            if (entity.isEmpty()) return Either.left(Notification.create(new Error("Partner not found: " + input)));
            return Either.right(GetPartnerOutput.from(entity.get()));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
