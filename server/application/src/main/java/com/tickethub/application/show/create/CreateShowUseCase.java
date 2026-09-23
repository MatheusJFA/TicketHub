package com.tickethub.application.show.create;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class CreateShowUseCase extends UseCase<CreateShowCommand, Either<Notification, CreateShowOutput>> {}
