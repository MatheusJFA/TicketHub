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
        Objects.requireNonNull(input);
        try {
            final var id = SectionID.from(input);
            sectionGateway.deleteById(id);
            return Either.right(new DeleteSectionOutput(input));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
