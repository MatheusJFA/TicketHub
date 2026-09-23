package com.tickethub.application.show.reschedule;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class RescheduleShowUseCase
        extends UseCase<RescheduleShowCommand, Either<Notification, RescheduleShowOutput>> {}
