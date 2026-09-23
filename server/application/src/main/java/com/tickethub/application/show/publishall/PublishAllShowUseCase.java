package com.tickethub.application.show.publishall;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class PublishAllShowUseCase
        extends UseCase<PublishAllShowCommand, Either<Notification, PublishAllShowOutput>> {}
