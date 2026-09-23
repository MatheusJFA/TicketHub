package com.tickethub.application.section.unpublish;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class UnpublishSectionUseCase
        extends UseCase<UnpublishSectionCommand, Either<Notification, UnpublishSectionOutput>> {}
