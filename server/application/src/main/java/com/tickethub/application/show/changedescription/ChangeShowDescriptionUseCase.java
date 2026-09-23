package com.tickethub.application.show.changedescription;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class ChangeShowDescriptionUseCase
        extends UseCase<ChangeShowDescriptionCommand, Either<Notification, ChangeShowDescriptionOutput>> {}
