package com.tickethub.application.section.changename;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class ChangeSectionNameUseCase
        extends UseCase<ChangeSectionNameCommand, Either<Notification, ChangeSectionNameOutput>> {}
