package com.tickethub.application.spot.retrieve.list;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.validation.Notification;

public abstract class ListSpotsUseCase
        extends UseCase<SearchQuery, Either<Notification, Pagination<ListSpotsOutput>>> {}
