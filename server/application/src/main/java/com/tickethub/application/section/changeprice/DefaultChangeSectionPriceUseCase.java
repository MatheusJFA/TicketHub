package com.tickethub.application.section.changeprice;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.validation.Error;

public final class DefaultChangeSectionPriceUseCase extends ChangeSectionPriceUseCase {
    private final SectionGateway sectionGateway;

    public DefaultChangeSectionPriceUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = Objects.requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, ChangeSectionPriceOutput> execute(final ChangeSectionPriceCommand input) {
        Objects.requireNonNull(input);
        try {
            final var id = SectionID.from(input.id());
            final var found = sectionGateway.findById(id);
            if (found.isEmpty()) {
                return Either.left(Notification.create(new Error("Section not found: " + input.id())));
            }
            final var entity = found.get();
            entity.changePrice(input.price());
            final var notification = Notification.create();
            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }
            final var saved = sectionGateway.update(entity);
            final var output = new ChangeSectionPriceOutput(saved.getId().getValue());
            return Either.right(output);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
