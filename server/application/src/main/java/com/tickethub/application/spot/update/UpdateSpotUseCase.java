package com.tickethub.application.spot.update;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class UpdateSpotUseCase extends UseCase<UpdateSpotCommand, Either<Notification, UpdateSpotOutput>> {
}
