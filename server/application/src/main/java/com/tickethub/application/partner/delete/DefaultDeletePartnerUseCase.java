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
        Objects.requireNonNull(input);
        try {
            partnerGateway.deleteById(PartnerID.from(input));
            return Either.right(new DeletePartnerOutput(input));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
