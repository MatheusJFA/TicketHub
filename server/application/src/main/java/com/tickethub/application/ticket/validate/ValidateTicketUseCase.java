package com.tickethub.application.ticket.validate;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class ValidateTicketUseCase
        extends UseCase<ValidateTicketCommand, Either<Notification, ValidateTicketOutput>> {
}
