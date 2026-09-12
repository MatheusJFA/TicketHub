package com.tickethub.application.partner.delete;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class DeletePartnerUseCase extends UseCase<String, Either<Notification, DeletePartnerOutput>> {
}
