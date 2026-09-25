package com.tickethub.application.partner.changewebhook;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class ChangePartnerWebhookUseCase
        extends UseCase<ChangePartnerWebhookCommand, Either<Notification, ChangePartnerWebhookOutput>> {}
