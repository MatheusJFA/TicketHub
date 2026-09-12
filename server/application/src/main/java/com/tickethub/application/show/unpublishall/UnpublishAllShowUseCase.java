package com.tickethub.application.show.unpublishall;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class UnpublishAllShowUseCase extends UseCase<UnpublishAllShowCommand, Either<Notification, UnpublishAllShowOutput>> {
}
