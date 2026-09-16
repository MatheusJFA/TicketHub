package com.tickethub.application.section.delete;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;


public final class DefaultDeleteSectionUseCase extends DeleteSectionUseCase {
    private final SectionGateway sectionGateway;

    public DefaultDeleteSectionUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = Objects.requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, DeleteSectionOutput> execute(final String input) {
        try {
            final SectionID id = SectionID.from(input);
            
            sectionGateway.deleteById(id);

            final DeleteSectionOutput output = DeleteSectionOutput.from(input);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
