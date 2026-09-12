package com.tickethub.application.partner.retrieve.get;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class GetPartnerUseCase extends UseCase<String, Either<Notification, GetPartnerOutput>> {
}
