package com.tickethub.application.show.retrieve.get;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class GetShowUseCase extends UseCase<String, Either<Notification, GetShowOutput>> {}
