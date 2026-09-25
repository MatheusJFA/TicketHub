package com.tickethub.application.coupon.create;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.coupon.Coupon;
import com.tickethub.domain.core.coupon.CouponGateway;
import com.tickethub.domain.core.coupon.CouponKind;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.shared.Money;
import com.tickethub.domain.validation.Notification;
import java.util.Currency;

public class DefaultCreateCouponUseCase extends CreateCouponUseCase {
    private final CouponGateway couponGateway;

    public DefaultCreateCouponUseCase(final CouponGateway couponGateway) {
        this.couponGateway = requireNonNull(couponGateway);
    }

    @Override
    public Either<Notification, CreateCouponOutput> execute(final CreateCouponCommand command) {
        try {
            final Coupon entity = Coupon.create(
                    command.code(),
                    command.showId(),
                    command.sectionId(),
                    command.kind(),
                    command.percent(),
                    fixedValue(command),
                    command.validFrom(),
                    command.validUntil(),
                    command.maxUses());

            final Notification notification = Notification.create();
            entity.validate(notification);

            if (notification.hasError()) {
                return Either.left(notification);
            }

            final Coupon createdCoupon = couponGateway.create(entity);
            final CreateCouponOutput output = CreateCouponOutput.from(createdCoupon);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

    private static Money fixedValue(final CreateCouponCommand command) {
        if (command.kind() != CouponKind.FIXED) {
            return null;
        }
        if (command.fixedValue() == null || command.fixedCurrency() == null) {
            throw new DomainException("'fixedValue' and 'fixedCurrency' are required for fixed coupons");
        }
        return Money.create(command.fixedValue(), Currency.getInstance(command.fixedCurrency()));
    }
}
