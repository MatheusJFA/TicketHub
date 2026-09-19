package com.tickethub.application.show.update;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class UpdateShowUseCase extends UseCase<UpdateShowCommand, Either<Notification, UpdateShowOutput>> {
}
