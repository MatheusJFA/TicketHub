package com.tickethub.application.coupon.create;

import com.tickethub.domain.core.coupon.Coupon;

public record CreateCouponOutput(String id, String code) {
    public static CreateCouponOutput from(final Coupon entity) {
        return new CreateCouponOutput(entity.getId().getValue(), entity.getCode());
    }
}
