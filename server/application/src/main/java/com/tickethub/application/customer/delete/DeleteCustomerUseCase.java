package com.tickethub.application.customer.delete;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class DeleteCustomerUseCase extends UseCase<String, Either<Notification, DeleteCustomerOutput>> {
}
