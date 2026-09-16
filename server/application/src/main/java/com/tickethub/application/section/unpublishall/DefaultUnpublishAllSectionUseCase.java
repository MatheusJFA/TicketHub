package com.tickethub.application.section.unpublishall;

import com.tickethub.domain.core.section.Section;

import java.util.Objects;
import java.util.Optional;

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
        try {
            final Optional<Section> found = sectionGateway.findById(SectionID.from(command.id()));

            if (!found.isPresent()) {
                return Either.left(notFound("Section", command.id()));
            }

            final Section entity = found.get();
            final Notification notification = Notification.create();

            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            entity.unpublishAll();

            return Either.right(UnpublishAllSectionOutput.from(sectionGateway.update(entity)));
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
