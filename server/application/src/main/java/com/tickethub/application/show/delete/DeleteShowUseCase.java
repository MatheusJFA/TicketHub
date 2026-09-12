package com.tickethub.application.show.delete;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class DeleteShowUseCase extends UseCase<String, Either<Notification, DeleteShowOutput>> {
}
