package com.tickethub.application.section.changename;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.validation.Notification;
import java.util.Optional;

public class DefaultChangeSectionNameUseCase extends ChangeSectionNameUseCase {
    private final SectionGateway sectionGateway;

    public DefaultChangeSectionNameUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, ChangeSectionNameOutput> execute(final ChangeSectionNameCommand input) {
        try {
            final SectionID id = SectionID.from(input.id());
            final Optional<Section> found = sectionGateway.findById(id);

            if (found.isEmpty()) {
                return Either.left(notFound(Section.class.getSimpleName(), id.getValue()));
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
