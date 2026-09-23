package com.tickethub.application.partner.changename;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class ChangePartnerNameUseCase
        extends UseCase<ChangePartnerNameCommand, Either<Notification, ChangePartnerNameOutput>> {}
