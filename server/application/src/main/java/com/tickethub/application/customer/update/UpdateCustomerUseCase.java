package com.tickethub.application.customer.update;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class UpdateCustomerUseCase extends UseCase<UpdateCustomerCommand, Either<Notification, UpdateCustomerOutput>> {
}
