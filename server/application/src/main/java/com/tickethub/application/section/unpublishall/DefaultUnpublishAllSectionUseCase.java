package com.tickethub.application.section.unpublishall;

import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

public final class DefaultUnpublishAllSectionUseCase extends UnpublishAllSectionUseCase {
    private final SectionGateway sectionGateway;

    public DefaultUnpublishAllSectionUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = Objects.requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, UnpublishAllSectionOutput> execute(final UnpublishAllSectionCommand command) {
        Objects.requireNonNull(command);
        try {
            final var found = sectionGateway.findById(SectionID.from(command.id()));

            if (!found.isPresent()) {
                return Either.left(Notification.create(new Error("Section not found: " + command.id())));
            }

            final var entity = found.get();
            final var notification = Notification.create();

            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            entity.unpublishAll();

            return Either.right(UnpublishAllSectionOutput.from(sectionGateway.update(entity)));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
