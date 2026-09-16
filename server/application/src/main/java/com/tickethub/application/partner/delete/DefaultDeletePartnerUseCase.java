package com.tickethub.application.partner.delete;
import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;


public final class DefaultDeletePartnerUseCase extends DeletePartnerUseCase {
    private final PartnerGateway partnerGateway;

    public DefaultDeletePartnerUseCase(final PartnerGateway partnerGateway) {
        this.partnerGateway = Objects.requireNonNull(partnerGateway);
    }

    @Override
    public Either<Notification, DeletePartnerOutput> execute(final String input) {
        try {
            final PartnerID id = PartnerID.from(input);
            partnerGateway.deleteById(id);

            final DeletePartnerOutput output = DeletePartnerOutput.from(input);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
