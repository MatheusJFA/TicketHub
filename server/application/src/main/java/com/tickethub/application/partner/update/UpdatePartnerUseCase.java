package com.tickethub.application.partner.update;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class UpdatePartnerUseCase
        extends UseCase<UpdatePartnerCommand, Either<Notification, UpdatePartnerOutput>> {}
