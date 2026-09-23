package com.tickethub.application.partner.create;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class CreatePartnerUseCase
        extends UseCase<CreatePartnerCommand, Either<Notification, CreatePartnerOutput>> {}
