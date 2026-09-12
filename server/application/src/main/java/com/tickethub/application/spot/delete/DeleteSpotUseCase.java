package com.tickethub.application.spot.delete;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class DeleteSpotUseCase extends UseCase<String, Either<Notification, DeleteSpotOutput>> {
}
