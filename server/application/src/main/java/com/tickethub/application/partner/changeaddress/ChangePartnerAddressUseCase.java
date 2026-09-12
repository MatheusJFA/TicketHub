package com.tickethub.application.partner.changeaddress;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class ChangePartnerAddressUseCase extends UseCase<ChangePartnerAddressCommand, Either<Notification, ChangePartnerAddressOutput>> {
}
