package com.tickethub.application.customer.changename;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class ChangeCustomerNameUseCase extends UseCase<ChangeCustomerNameCommand, Either<Notification, ChangeCustomerNameOutput>> {
}
