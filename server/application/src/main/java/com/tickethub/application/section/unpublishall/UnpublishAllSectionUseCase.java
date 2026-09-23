package com.tickethub.application.section.unpublishall;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class UnpublishAllSectionUseCase
        extends UseCase<UnpublishAllSectionCommand, Either<Notification, UnpublishAllSectionOutput>> {}
