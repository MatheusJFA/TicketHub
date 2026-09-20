package com.tickethub.application.section.retrieve.get;

import static java.util.Objects.requireNonNull;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.validation.Notification;

public class DefaultGetSectionUseCase extends GetSectionUseCase {
    private final SectionGateway sectionGateway;

    public DefaultGetSectionUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, GetSectionOutput> execute(final String input) {
        try {
            final SectionID id = SectionID.from(input);

            final Optional<Section> found = sectionGateway.findById(id);
            if (found.isEmpty()) {
                return Either.left(notFound(Section.class.getSimpleName(), id.getValue()));
            }

            final Section entity = found.get();
            final GetSectionOutput output = GetSectionOutput.from(entity);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception); 
            return Either.left(notification);
        }
    }
    
}
