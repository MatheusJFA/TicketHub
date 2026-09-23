package com.tickethub.application.show.update;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class UpdateShowUseCase extends UseCase<UpdateShowCommand, Either<Notification, UpdateShowOutput>> {}
