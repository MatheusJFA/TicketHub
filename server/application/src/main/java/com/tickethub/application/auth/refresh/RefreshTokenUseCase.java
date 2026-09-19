package com.tickethub.application.auth.refresh;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class RefreshTokenUseCase extends UseCase<RefreshTokenCommand, Either<Notification, RefreshTokenOutput>> {
}
