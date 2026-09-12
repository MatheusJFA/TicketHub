package com.tickethub.application.partner.create;

import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

public final class DefaultCreatePartnerUseCase extends CreatePartnerUseCase {
    private final PartnerGateway partnerGateway;

    public DefaultCreatePartnerUseCase(final PartnerGateway partnerGateway) {
        this.partnerGateway = Objects.requireNonNull(partnerGateway);
    }

    @Override
    public Either<Notification, CreatePartnerOutput> execute(final CreatePartnerCommand command) {
        Objects.requireNonNull(command);
        try {
            final var entity = Partner.create(command.name(), command.cnpj(), command.address());
            final var notification = Notification.create();
            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }
            return Either.right(CreatePartnerOutput.from(partnerGateway.create(entity)));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
