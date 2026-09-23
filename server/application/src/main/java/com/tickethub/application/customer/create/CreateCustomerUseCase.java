package com.tickethub.application.customer.create;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class CreateCustomerUseCase
        extends UseCase<CreateCustomerCommand, Either<Notification, CreateCustomerOutput>> {}
