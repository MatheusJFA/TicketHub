package com.tickethub.application.show.unpublish;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class UnpublishShowUseCase extends UseCase<UnpublishShowCommand, Either<Notification, UnpublishShowOutput>> {
}
