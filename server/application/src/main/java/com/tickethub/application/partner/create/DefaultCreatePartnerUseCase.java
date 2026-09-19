package com.tickethub.application.partner.create;

import static java.util.Objects.isNull;

import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.auth.PasswordHasher;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.geo.CepLookup;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.validation.Notification;

public class DefaultCreatePartnerUseCase extends CreatePartnerUseCase {
    private final PartnerGateway partnerGateway;
    private final PasswordHasher passwordHasher;
    private final CepLookup cepLookup;

    public DefaultCreatePartnerUseCase(final PartnerGateway partnerGateway, final PasswordHasher passwordHasher,
            final CepLookup cepLookup) {
        this.partnerGateway = Objects.requireNonNull(partnerGateway);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.cepLookup = Objects.requireNonNull(cepLookup);
    }

    @Override
    public Either<Notification, CreatePartnerOutput> execute(final CreatePartnerCommand command) {
        try {
            final Partner entity = Partner.create(
                command.name(),
                command.cnpj(),
                enrich(command.address()),
                command.email(),
                passwordHasher.hash(command.password())
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

    private Address enrich(final Address address) {
        if (isNull(address)) {
            return null;
        }
        return cepLookup.lookup(address.getZipCode()).map(address::enrichedWith).orElse(address);
    }
}
