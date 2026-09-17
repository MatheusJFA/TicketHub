package com.tickethub.application.partner.changeaddress;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.validation.Notification;

public final class DefaultChangePartnerAddressUseCase extends ChangePartnerAddressUseCase {
    private final PartnerGateway partnerGateway;

    public DefaultChangePartnerAddressUseCase(final PartnerGateway partnerGateway) {
        this.partnerGateway = Objects.requireNonNull(partnerGateway);
    }

    @Override
    public Either<Notification, ChangePartnerAddressOutput> execute(final ChangePartnerAddressCommand input) {
        try {
            final PartnerID id = PartnerID.from(input.id());

            final Optional<Partner> found = partnerGateway.findById(id);
            if (!found.isPresent()) {
                return Either.left(notFound(Partner.class.getSimpleName(), id.getValue()));
            }

            final Partner entity = found.get();
            entity.changeAddress(input.address());

            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Partner updatedCategory = partnerGateway.update(entity);
            final ChangePartnerAddressOutput output = ChangePartnerAddressOutput.from(updatedCategory);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
