package com.tickethub.application.partner.reject;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.validation.Notification;
import java.util.Optional;

public class DefaultRejectPartnerUseCase extends RejectPartnerUseCase {
    private final PartnerGateway partnerGateway;

    public DefaultRejectPartnerUseCase(final PartnerGateway partnerGateway) {
        this.partnerGateway = requireNonNull(partnerGateway);
    }

    @Override
    public Either<Notification, RejectPartnerOutput> execute(final String input) {
        try {
            final PartnerID id = PartnerID.from(input);
            final Optional<Partner> found = partnerGateway.findById(id);

            if (found.isEmpty()) {
                return Either.left(notFound(Partner.class.getSimpleName(), id.getValue()));
            }

            final Partner entity = found.get().reject();

            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Partner saved = partnerGateway.update(entity);
            return Either.right(RejectPartnerOutput.from(saved));
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
