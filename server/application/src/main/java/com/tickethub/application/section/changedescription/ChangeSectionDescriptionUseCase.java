package com.tickethub.application.section.changedescription;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class ChangeSectionDescriptionUseCase
        extends UseCase<ChangeSectionDescriptionCommand, Either<Notification, ChangeSectionDescriptionOutput>> {}
