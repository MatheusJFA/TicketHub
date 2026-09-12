package com.tickethub.application.spot.create;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class CreateSpotUseCase extends UseCase<CreateSpotCommand, Either<Notification, CreateSpotOutput>> {
}
