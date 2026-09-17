package com.tickethub.application.section.publishall;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.validation.Notification;

public final class DefaultPublishAllSectionUseCase extends PublishAllSectionUseCase {
    private final SectionGateway sectionGateway;

    public DefaultPublishAllSectionUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = Objects.requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, PublishAllSectionOutput> execute(final PublishAllSectionCommand command) {
        try {
            final SectionID id = SectionID.from(command.id());

            final Optional<Section> found = sectionGateway.findById(id);
            if (!found.isPresent()) {
                return Either.left(notFound(Section.class.getSimpleName(), id.getValue()));
            }

            final Section entity = found.get();

            final Notification notification = Notification.create();
            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }

            entity.publishAll();

            final Section updatedSection = sectionGateway.update(entity);
            final PublishAllSectionOutput output = PublishAllSectionOutput.from(updatedSection);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
