package com.tickethub.application.section.unpublishall;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.validation.Notification;

public class DefaultUnpublishAllSectionUseCase extends UnpublishAllSectionUseCase {
    private final SectionGateway sectionGateway;

    public DefaultUnpublishAllSectionUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = Objects.requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, UnpublishAllSectionOutput> execute(final UnpublishAllSectionCommand command) {
        try {
            final SectionID id = SectionID.from(command.id());
            final Optional<Section> found = sectionGateway.findById(id);

            if (found.isEmpty()) {
                return Either.left(notFound(Section.class.getSimpleName(), id.getValue()));
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
