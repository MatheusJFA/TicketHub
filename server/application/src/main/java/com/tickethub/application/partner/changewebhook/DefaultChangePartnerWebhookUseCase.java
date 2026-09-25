package com.tickethub.application.partner.changewebhook;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.validation.Notification;
import java.util.Optional;

public class DefaultChangePartnerWebhookUseCase extends ChangePartnerWebhookUseCase {
    private final PartnerGateway partnerGateway;

    public DefaultChangePartnerWebhookUseCase(final PartnerGateway partnerGateway) {
        this.partnerGateway = requireNonNull(partnerGateway);
    }

    @Override
    public Either<Notification, ChangePartnerWebhookOutput> execute(final ChangePartnerWebhookCommand input) {
        try {
            final PartnerID id = PartnerID.from(input.id());
            final Optional<Partner> found = partnerGateway.findById(id);

            if (found.isEmpty()) {
                return Either.left(notFound(Partner.class.getSimpleName(), id.getValue()));
            }

            final Partner entity = found.get();
            entity.changeWebhook(input.webhookUrl(), input.webhookSecret());

            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Partner saved = partnerGateway.update(entity);
            final ChangePartnerWebhookOutput output = ChangePartnerWebhookOutput.from(saved);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
