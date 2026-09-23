package com.tickethub.application.customer.retrieve.get;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class GetCustomerUseCase extends UseCase<String, Either<Notification, GetCustomerOutput>> {}
