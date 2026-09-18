package com.tickethub.application.partner.retrieve.get;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.validation.Notification;

public class DefaultGetPartnerUseCase extends GetPartnerUseCase {
    private final PartnerGateway partnerGateway;

    public DefaultGetPartnerUseCase(final PartnerGateway partnerGateway) {
        this.partnerGateway = Objects.requireNonNull(partnerGateway);
    }

    @Override
    public Either<Notification, GetPartnerOutput> execute(final String input) {
        try {
            final PartnerID id = PartnerID.from(input);
            final Optional<Partner> entity = partnerGateway.findById(id);

            if (!entity.isPresent()) {
                return Either.left(notFound(Partner.class.getSimpleName(), id.getValue()));
            }

            final Partner partner = entity.get();

            final GetPartnerOutput output = GetPartnerOutput.from(partner);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
