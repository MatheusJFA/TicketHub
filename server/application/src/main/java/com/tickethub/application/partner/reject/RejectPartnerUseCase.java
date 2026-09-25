package com.tickethub.application.partner.reject;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class RejectPartnerUseCase extends UseCase<String, Either<Notification, RejectPartnerOutput>> {}
