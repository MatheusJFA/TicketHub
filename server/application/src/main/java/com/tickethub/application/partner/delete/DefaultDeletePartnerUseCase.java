package com.tickethub.application.partner.delete;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.validation.Notification;
import java.util.Optional;

public class DefaultDeletePartnerUseCase extends DeletePartnerUseCase {
    private final PartnerGateway partnerGateway;

    public DefaultDeletePartnerUseCase(final PartnerGateway partnerGateway) {
        this.partnerGateway = requireNonNull(partnerGateway);
    }

    @Override
    public Optional<Notification> execute(final String input) {
        try {
            final PartnerID id = PartnerID.from(input);
            partnerGateway.deleteById(id);
            return Optional.empty();
        } catch (final RuntimeException exception) {
            return Optional.of(Notification.create(exception));
        }
    }
}
