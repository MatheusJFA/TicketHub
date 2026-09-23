package com.tickethub.application.spot.update;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class UpdateSpotUseCase extends UseCase<UpdateSpotCommand, Either<Notification, UpdateSpotOutput>> {}
