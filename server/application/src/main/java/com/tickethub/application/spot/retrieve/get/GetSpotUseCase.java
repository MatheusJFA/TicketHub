package com.tickethub.application.spot.retrieve.get;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class GetSpotUseCase extends UseCase<String, Either<Notification, GetSpotOutput>> {
}
