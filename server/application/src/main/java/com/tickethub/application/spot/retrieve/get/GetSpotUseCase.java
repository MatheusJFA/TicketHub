package com.tickethub.application.spot.retrieve.get;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class GetSpotUseCase extends UseCase<String, Either<Notification, GetSpotOutput>> {}
