package com.tickethub.application.section.retrieve.byshow;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.application.section.retrieve.list.ListSectionsOutput;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.validation.Notification;

public class DefaultListShowSectionsUseCase extends ListShowSectionsUseCase {
    private final SectionGateway sectionGateway;

    public DefaultListShowSectionsUseCase(final SectionGateway sectionGateway) {
        this.sectionGateway = requireNonNull(sectionGateway);
    }

    @Override
    public Either<Notification, Pagination<ListSectionsOutput>> execute(final ListShowSectionsCommand input) {
        try {
            final Pagination<Section> page =
                    sectionGateway.findByShowId(ShowID.from(input.showId()), input.query());
            return Either.right(page.map(ListSectionsOutput::from));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
