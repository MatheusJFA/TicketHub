package com.tickethub.application.customer.retrieve.list;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.application.UseCase;

public abstract class ListCustomersUseCase extends UseCase<SearchQuery, Either<Notification, Pagination<ListCustomersOutput>>> {
}
