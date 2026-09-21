package com.tickethub.application.order.pay;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class PayOrderUseCase extends UseCase<PayOrderCommand, Either<Notification, PayOrderOutput>> {
}
