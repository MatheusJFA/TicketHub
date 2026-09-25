package com.tickethub.application.coupon.create;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class CreateCouponUseCase
        extends UseCase<CreateCouponCommand, Either<Notification, CreateCouponOutput>> {}
