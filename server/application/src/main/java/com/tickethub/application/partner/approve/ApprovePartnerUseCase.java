package com.tickethub.application.partner.approve;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class ApprovePartnerUseCase extends UseCase<String, Either<Notification, ApprovePartnerOutput>> {}
