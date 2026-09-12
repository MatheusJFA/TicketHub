package com.tickethub.application.show.changename;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class ChangeShowNameUseCase extends UseCase<ChangeShowNameCommand, Either<Notification, ChangeShowNameOutput>> {
}
