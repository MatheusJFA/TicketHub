package com.tickethub.application.section.retrieve.get;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.validation.Error;

public final class DefaultGetSectionUseCase extends GetSectionUseCase {
    private final SectionGateway sectionGateway;

    public DefaultGetSectionUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = Objects.requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, GetSectionOutput> execute(final String input) {
        Objects.requireNonNull(input);
        try {
            final var id = SectionID.from(input);
            final var found = sectionGateway.findById(id);
            if (found.isEmpty()) {
                return Either.left(Notification.create(new Error("Section not found: " + input)));
            }
            final var entity = found.get();
            final var output = GetSectionOutput.from(entity);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
