package com.tickethub.application.section.delete;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.validation.Notification;
import java.util.Optional;

public class DefaultDeleteSectionUseCase extends DeleteSectionUseCase {
    private final SectionGateway sectionGateway;

    public DefaultDeleteSectionUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = requireNonNull(sectionGateway);
    }

    @Override
    public Optional<Notification> execute(final String input) {
        try {
            final SectionID id = SectionID.from(input);
            sectionGateway.deleteById(id);
            return Optional.empty();
        } catch (final RuntimeException exception) {
            return Optional.of(Notification.create(exception));
        }
    }
}
