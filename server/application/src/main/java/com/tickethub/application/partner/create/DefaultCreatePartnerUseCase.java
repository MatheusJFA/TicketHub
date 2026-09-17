package com.tickethub.application.partner.create;

import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.validation.Notification;

public final class DefaultCreatePartnerUseCase extends CreatePartnerUseCase {
    private final PartnerGateway partnerGateway;

    public DefaultCreatePartnerUseCase(final PartnerGateway partnerGateway) {
        this.partnerGateway = Objects.requireNonNull(partnerGateway);
    }

    @Override
    public Either<Notification, CreatePartnerOutput> execute(final CreatePartnerCommand command) {
        try {
            final Partner entity = Partner.create(
                command.name(), 
                command.cnpj(), 
                command.address()
            );
            
            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Partner createdPartner = partnerGateway.create(entity);
            final CreatePartnerOutput output = CreatePartnerOutput.from(createdPartner);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
