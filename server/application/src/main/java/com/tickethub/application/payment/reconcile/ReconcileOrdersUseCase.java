package com.tickethub.application.payment.reconcile;

import com.tickethub.application.Either;
import com.tickethub.application.NullaryUseCase;
import com.tickethub.domain.validation.Notification;

public abstract class ReconcileOrdersUseCase extends NullaryUseCase<Either<Notification, ReconcileOrdersOutput>> {}
