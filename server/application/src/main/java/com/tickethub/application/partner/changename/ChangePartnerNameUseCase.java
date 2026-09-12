package com.tickethub.application.partner.changename;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class ChangePartnerNameUseCase extends UseCase<ChangePartnerNameCommand, Either<Notification, ChangePartnerNameOutput>> {
}
