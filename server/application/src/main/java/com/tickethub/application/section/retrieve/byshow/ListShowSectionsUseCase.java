package com.tickethub.application.section.retrieve.byshow;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.validation.Notification;
import com.tickethub.application.section.retrieve.list.ListSectionsOutput;

public abstract class ListShowSectionsUseCase
        extends UseCase<ListShowSectionsCommand, Either<Notification, Pagination<ListSectionsOutput>>> {
}
