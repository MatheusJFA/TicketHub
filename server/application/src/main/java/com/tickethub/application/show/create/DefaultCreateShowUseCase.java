package com.tickethub.application.show.create;

import static java.util.Objects.isNull;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.geo.CepLookup;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.validation.Notification;

public class DefaultCreateShowUseCase extends CreateShowUseCase {
    private final ShowGateway showGateway;
    private final PartnerGateway partnerGateway;
    private final CepLookup cepLookup;

    public DefaultCreateShowUseCase(final ShowGateway showGateway, final PartnerGateway partnerGateway,
            final CepLookup cepLookup) {
        this.showGateway = Objects.requireNonNull(showGateway);
        this.partnerGateway = Objects.requireNonNull(partnerGateway);
        this.cepLookup = Objects.requireNonNull(cepLookup);
    }

    @Override
    public Either<Notification, CreateShowOutput> execute(final CreateShowCommand command) {
        try {
            final PartnerID partnerId = PartnerID.from(command.partnerId());

            final Optional<Partner> entity = partnerGateway.findById(partnerId);

            if (!entity.isPresent()) {
                return Either.left(notFound(Partner.class.getSimpleName(), partnerId.getValue()));
            }

            final Partner partner = entity.get();
            final Show show = partner.createShow(
                    command.name(),
                    command.description(),
                    command.date(),
                    enrich(command.address()),
                    command.totalSpots());

            final Notification notification = Notification.create();
            show.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Show savedEntity = showGateway.create(show);
            final CreateShowOutput output = CreateShowOutput.from(savedEntity);
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
