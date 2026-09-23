package com.tickethub.application.section.create;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class CreateSectionUseCase
        extends UseCase<CreateSectionCommand, Either<Notification, CreateSectionOutput>> {}
