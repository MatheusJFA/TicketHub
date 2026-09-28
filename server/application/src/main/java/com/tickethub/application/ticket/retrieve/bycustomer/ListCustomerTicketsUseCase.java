package com.tickethub.application.ticket.retrieve.bycustomer;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;
import java.util.List;

public abstract class ListCustomerTicketsUseCase
        extends UseCase<ListCustomerTicketsCommand, Either<Notification, List<ListCustomerTicketsOutput>>> {}
