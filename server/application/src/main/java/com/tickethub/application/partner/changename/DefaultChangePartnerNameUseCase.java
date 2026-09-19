package com.tickethub.application.partner.changename;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.validation.Notification;

public class DefaultChangePartnerNameUseCase extends ChangePartnerNameUseCase {
    private final PartnerGateway partnerGateway;

    public DefaultChangePartnerNameUseCase(final PartnerGateway partnerGateway) {
        this.partnerGateway = Objects.requireNonNull(partnerGateway);
    }

    @Override
    public Either<Notification, ChangePartnerNameOutput> execute(final ChangePartnerNameCommand input) {
        try {
            final PartnerID id = PartnerID.from(input.id());
            final Optional<Partner> found = partnerGateway.findById(id);

            if (found.isEmpty()) {
                return Either.left(notFound(Partner.class.getSimpleName(), id.getValue()));
            }

            final Partner entity = found.get();
            entity.changeName(input.name());

            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Partner updatedPartner = partnerGateway.update(entity);
            final ChangePartnerNameOutput output = ChangePartnerNameOutput.from(updatedPartner);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
