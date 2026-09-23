package com.tickethub.application.show.addsection;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class AddSectionToShowUseCase
        extends UseCase<AddSectionToShowCommand, Either<Notification, AddSectionToShowOutput>> {}
