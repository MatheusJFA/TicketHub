package com.tickethub.application.section.retrieve.list;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.Section;



public final class DefaultListSectionsUseCase extends ListSectionsUseCase {
    private final SectionGateway sectionGateway;

    public DefaultListSectionsUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = Objects.requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, Pagination<ListSectionsOutput>> execute(final SearchQuery input) {
        try {
            final Pagination<Section> page = sectionGateway.findAll(input);
            final Pagination<ListSectionsOutput> output = page.map(ListSectionsOutput::from);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
