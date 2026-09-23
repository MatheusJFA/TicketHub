package com.tickethub.application.spot.publish;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class PublishSpotUseCase extends UseCase<PublishSpotCommand, Either<Notification, PublishSpotOutput>> {}
