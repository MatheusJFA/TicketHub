package com.tickethub.application.section.create;

import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

public final class DefaultCreateSectionUseCase extends CreateSectionUseCase {
    private final SectionGateway sectionGateway;

    public DefaultCreateSectionUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = Objects.requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, CreateSectionOutput> execute(final CreateSectionCommand command) {
        try {
            final Section entity = Section.create(
                command.name(), 
                command.description(), 
                command.totalSpots(), 
                command.price()
            );

            final Notification notification = Notification.create();
           
            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Section savedSection = sectionGateway.create(entity);
            final CreateSectionOutput output = CreateSectionOutput.from(savedSection);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
