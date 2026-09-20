package com.tickethub.application.authentication.login;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class LoginUseCase extends UseCase<LoginCommand, Either<Notification, LoginOutput>> {
}
