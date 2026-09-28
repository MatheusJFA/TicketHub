package com.tickethub.application.ticket.retrieve.byshow;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;
import java.util.List;

public abstract class ListShowTicketsUseCase
        extends UseCase<ListShowTicketsCommand, Either<Notification, List<ListShowTicketsOutput>>> {}
