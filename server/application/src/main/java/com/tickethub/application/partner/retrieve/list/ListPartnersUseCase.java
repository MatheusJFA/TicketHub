package com.tickethub.application.partner.retrieve.list;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.validation.Notification;

public abstract class ListPartnersUseCase
        extends UseCase<SearchQuery, Either<Notification, Pagination<ListPartnersOutput>>> {}
