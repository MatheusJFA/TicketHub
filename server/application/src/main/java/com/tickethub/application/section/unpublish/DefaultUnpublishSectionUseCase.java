package com.tickethub.application.section.unpublish;

import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

public final class DefaultUnpublishSectionUseCase extends UnpublishSectionUseCase {
    private final SectionGateway sectionGateway;

    public DefaultUnpublishSectionUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = Objects.requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, UnpublishSectionOutput> execute(final UnpublishSectionCommand command) {
        Objects.requireNonNull(command);
        try {
            final var found = sectionGateway.findById(SectionID.from(command.id()));
            if (found.isEmpty()) {
                return Either.left(Notification.create(new Error("Section not found: " + command.id())));
            }
            final var entity = found.get();
            final var notification = Notification.create();
            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }
            entity.unpublish();
            return Either.right(UnpublishSectionOutput.from(sectionGateway.update(entity)));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
