package com.tickethub.application.operator.create;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class CreateOperatorUseCase
        extends UseCase<CreateOperatorCommand, Either<Notification, CreateOperatorOutput>> {}
