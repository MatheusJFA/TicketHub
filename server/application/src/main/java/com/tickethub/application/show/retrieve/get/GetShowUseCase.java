package com.tickethub.application.show.retrieve.get;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class GetShowUseCase extends UseCase<String, Either<Notification, GetShowOutput>> {
}
