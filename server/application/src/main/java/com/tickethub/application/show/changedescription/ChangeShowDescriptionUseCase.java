package com.tickethub.application.show.changedescription;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class ChangeShowDescriptionUseCase extends UseCase<ChangeShowDescriptionCommand, Either<Notification, ChangeShowDescriptionOutput>> {
}
