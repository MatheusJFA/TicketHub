package com.tickethub.application.order.create;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class CreateOrderUseCase extends UseCase<CreateOrderCommand, Either<Notification, CreateOrderOutput>> {}
