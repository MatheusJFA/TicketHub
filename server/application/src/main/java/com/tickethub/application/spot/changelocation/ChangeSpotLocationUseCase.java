package com.tickethub.application.spot.changelocation;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class ChangeSpotLocationUseCase extends UseCase<ChangeSpotLocationCommand, Either<Notification, ChangeSpotLocationOutput>> {
}
