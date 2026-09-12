package com.tickethub.application.partner.changeaddress;
import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.validation.Error;

public final class DefaultChangePartnerAddressUseCase extends ChangePartnerAddressUseCase {
    private final PartnerGateway partnerGateway;

    public DefaultChangePartnerAddressUseCase(final PartnerGateway partnerGateway) {
        this.partnerGateway = Objects.requireNonNull(partnerGateway);
    }

    @Override
    public Either<Notification, ChangePartnerAddressOutput> execute(final ChangePartnerAddressCommand input) {
        Objects.requireNonNull(input);
        try {
            final var found = partnerGateway.findById(PartnerID.from(input.id()));
            if (found.isEmpty()) return Either.left(Notification.create(new Error("Partner not found: " + input.id())));
            final var entity = found.get();
            entity.changeAddress(input.address());
            final var notification = Notification.create();
            entity.validate(notification);
            if (notification.hasError()) return Either.left(notification);
            return Either.right(ChangePartnerAddressOutput.from(partnerGateway.update(entity)));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
