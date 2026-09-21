package com.tickethub.application.order.cancel;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class CancelOrderUseCase extends UseCase<CancelOrderCommand, Either<Notification, CancelOrderOutput>> {
}
