package com.tickethub.application.section.changedescription;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.validation.Notification;

public final class DefaultChangeSectionDescriptionUseCase extends ChangeSectionDescriptionUseCase {
    private final SectionGateway sectionGateway;

    public DefaultChangeSectionDescriptionUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = Objects.requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, ChangeSectionDescriptionOutput> execute(final ChangeSectionDescriptionCommand input) {
        try {
            final SectionID id = SectionID.from(input.id());

            final Optional<Section> found = sectionGateway.findById(id);
            if (found.isEmpty()) {
                return Either.left(notFound(Section.class.getSimpleName(), id.getValue()));
            }

            final Section entity = found.get();
            entity.changeDescription(input.description());

            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Section saved = sectionGateway.update(entity);
            final ChangeSectionDescriptionOutput output = ChangeSectionDescriptionOutput.from(saved);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
