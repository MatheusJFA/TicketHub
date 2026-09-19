package com.tickethub.application.section.update;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.validation.Notification;

public class DefaultUpdateSectionUseCase extends UpdateSectionUseCase {
    private final SectionGateway sectionGateway;

    public DefaultUpdateSectionUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = Objects.requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, UpdateSectionOutput> execute(final UpdateSectionCommand input) {
        try {
            final SectionID id = SectionID.from(input.id());
            final Optional<Section> found = sectionGateway.findById(id);

            if (found.isEmpty()) {
                return Either.left(notFound(Section.class.getSimpleName(), id.getValue()));
            }

            final Section entity = found.get();
            entity.changeName(input.name());
            entity.changeDescription(input.description());
            entity.changePrice(input.price());

            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Section saved = sectionGateway.update(entity);
            final UpdateSectionOutput output = UpdateSectionOutput.from(saved);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
