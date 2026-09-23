package com.tickethub.application.spot.changelocation;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class ChangeSpotLocationUseCase
        extends UseCase<ChangeSpotLocationCommand, Either<Notification, ChangeSpotLocationOutput>> {}
