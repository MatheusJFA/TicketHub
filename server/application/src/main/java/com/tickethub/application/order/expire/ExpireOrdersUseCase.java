package com.tickethub.application.order.expire;

import com.tickethub.application.Either;
import com.tickethub.application.NullaryUseCase;
import com.tickethub.domain.validation.Notification;

public abstract class ExpireOrdersUseCase extends NullaryUseCase<Either<Notification, ExpireOrdersOutput>> {}
