package com.tickethub.application.order.retrieve.get;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class GetOrderUseCase extends UseCase<String, Either<Notification, GetOrderOutput>> {
}
