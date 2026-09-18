package com.tickethub.application.partner.delete;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;


public class DefaultDeletePartnerUseCase extends DeletePartnerUseCase {
    private final PartnerGateway partnerGateway;

    public DefaultDeletePartnerUseCase(final PartnerGateway partnerGateway) {
        this.partnerGateway = Objects.requireNonNull(partnerGateway);
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
