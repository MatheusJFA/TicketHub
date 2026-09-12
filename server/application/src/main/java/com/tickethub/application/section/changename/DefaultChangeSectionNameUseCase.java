package com.tickethub.application.section.changename;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.validation.Error;

public final class DefaultChangeSectionNameUseCase extends ChangeSectionNameUseCase {
    private final SectionGateway sectionGateway;

    public DefaultChangeSectionNameUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = Objects.requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, ChangeSectionNameOutput> execute(final ChangeSectionNameCommand input) {
        Objects.requireNonNull(input);
        try {
            final var id = SectionID.from(input.id());
            final var found = sectionGateway.findById(id);
            if (found.isEmpty()) {
                return Either.left(Notification.create(new Error("Section not found: " + input.id())));
            }
            final var entity = found.get();
            entity.changeName(input.name());
            final var notification = Notification.create();
            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }
            final var saved = sectionGateway.update(entity);
            final var output = new ChangeSectionNameOutput(saved.getId().getValue());
            return Either.right(output);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
