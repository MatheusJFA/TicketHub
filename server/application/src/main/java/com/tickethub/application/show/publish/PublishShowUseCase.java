package com.tickethub.application.show.publish;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class PublishShowUseCase extends UseCase<PublishShowCommand, Either<Notification, PublishShowOutput>> {}
