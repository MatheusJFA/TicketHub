package com.tickethub.application.spot.retrieve.bysection;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.application.spot.retrieve.list.ListSpotsOutput;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.validation.Notification;

public abstract class ListSectionSpotsUseCase
        extends UseCase<ListSectionSpotsCommand, Either<Notification, Pagination<ListSpotsOutput>>> {
}
