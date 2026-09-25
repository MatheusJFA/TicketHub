package com.tickethub.application.partner.approve;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.validation.Notification;
import java.util.Optional;

public class DefaultApprovePartnerUseCase extends ApprovePartnerUseCase {
    private final PartnerGateway partnerGateway;

    public DefaultApprovePartnerUseCase(final PartnerGateway partnerGateway) {
        this.partnerGateway = requireNonNull(partnerGateway);
    }

    @Override
    public Either<Notification, ApprovePartnerOutput> execute(final String input) {
        try {
            final PartnerID id = PartnerID.from(input);
            final Optional<Partner> found = partnerGateway.findById(id);

            if (found.isEmpty()) {
                return Either.left(notFound(Partner.class.getSimpleName(), id.getValue()));
            }

            final Partner entity = found.get().approve();

            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Partner saved = partnerGateway.update(entity);
            return Either.right(ApprovePartnerOutput.from(saved));
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
