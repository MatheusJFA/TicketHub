package com.tickethub.application.section.retrieve.list;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.validation.Notification;

public class DefaultListSectionsUseCase extends ListSectionsUseCase {
    private final SectionGateway sectionGateway;

    public DefaultListSectionsUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = requireNonNull(sectionGateway);
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
