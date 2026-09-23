package com.tickethub.application.section.update;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class UpdateSectionUseCase
        extends UseCase<UpdateSectionCommand, Either<Notification, UpdateSectionOutput>> {}
