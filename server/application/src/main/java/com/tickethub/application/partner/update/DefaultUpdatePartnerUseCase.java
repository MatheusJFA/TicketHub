package com.tickethub.application.partner.update;

import static java.util.Objects.isNull;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.geo.CepLookup;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.validation.Notification;

public class DefaultUpdatePartnerUseCase extends UpdatePartnerUseCase {
    private final PartnerGateway partnerGateway;
    private final CepLookup cepLookup;

    public DefaultUpdatePartnerUseCase(final PartnerGateway partnerGateway, final CepLookup cepLookup) {
        this.partnerGateway = Objects.requireNonNull(partnerGateway);
        this.cepLookup = Objects.requireNonNull(cepLookup);
    }

    @Override
    public Either<Notification, UpdatePartnerOutput> execute(final UpdatePartnerCommand input) {
        try {
            final PartnerID id = PartnerID.from(input.id());
            final Optional<Partner> found = partnerGateway.findById(id);

            if (found.isEmpty()) {
                return Either.left(notFound(Partner.class.getSimpleName(), id.getValue()));
            }

            final Partner entity = found.get();
            entity.changeName(input.name());
            entity.changeAddress(enrich(input.address()));

            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Partner saved = partnerGateway.update(entity);
            final UpdatePartnerOutput output = UpdatePartnerOutput.from(saved);
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
