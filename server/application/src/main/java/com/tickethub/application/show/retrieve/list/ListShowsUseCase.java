package com.tickethub.application.show.retrieve.list;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.application.UseCase;

public abstract class ListShowsUseCase extends UseCase<SearchQuery, Either<Notification, Pagination<ListShowsOutput>>> {
}
