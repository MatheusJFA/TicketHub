package com.tickethub.application.partner.create;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.authentication.PasswordHasher;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.validation.Notification;

public class DefaultCreatePartnerUseCase extends CreatePartnerUseCase {
    private final PartnerGateway partnerGateway;
    private final PasswordHasher passwordHasher;

    public DefaultCreatePartnerUseCase(final PartnerGateway partnerGateway, final PasswordHasher passwordHasher) {
        this.partnerGateway = requireNonNull(partnerGateway);
        this.passwordHasher = requireNonNull(passwordHasher);
    }

    @Override
    public Either<Notification, CreatePartnerOutput> execute(final CreatePartnerCommand command) {
        try {
            final Partner entity = Partner.create(
                    command.name(),
                    command.cnpj(),
                    command.address(),
                    command.email(),
                    passwordHasher.hash(command.password()));

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
