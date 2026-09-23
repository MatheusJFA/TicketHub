package com.tickethub.application.payment.confirm;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class ConfirmPaymentUseCase
        extends UseCase<ConfirmPaymentCommand, Either<Notification, ConfirmPaymentOutput>> {}
