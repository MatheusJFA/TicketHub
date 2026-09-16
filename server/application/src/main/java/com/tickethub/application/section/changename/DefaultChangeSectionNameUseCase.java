package com.tickethub.application.section.changename;

import com.tickethub.domain.core.section.Section;

import java.util.Objects;
import java.util.Optional;
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
        try {
            final SectionID id = SectionID.from(input.id());
            final Optional<Section> found = sectionGateway.findById(id);
            
            if (!found.isPresent()) {
                return Either.left(notFound("Section", input.id()));
            }

            final Section entity = found.get();
            entity.changeName(input.name());
            
            final Notification notification = Notification.create();
            entity.validate(notification);
            
            if (notification.hasError()) {
                return Either.left(notification);
            }
            
            final Section saved = sectionGateway.update(entity);
            final ChangeSectionNameOutput output = ChangeSectionNameOutput.from(saved);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
