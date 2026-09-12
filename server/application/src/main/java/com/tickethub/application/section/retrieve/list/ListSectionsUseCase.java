package com.tickethub.application.section.retrieve.list;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.application.UseCase;

public abstract class ListSectionsUseCase extends UseCase<SearchQuery, Either<Notification, Pagination<ListSectionsOutput>>> {
}
