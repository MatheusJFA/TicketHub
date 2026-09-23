package com.tickethub.application.partner.changeaddress;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class ChangePartnerAddressUseCase
        extends UseCase<ChangePartnerAddressCommand, Either<Notification, ChangePartnerAddressOutput>> {}
