package com.tickethub.domain.core.coupon;

import java.time.Instant;
import java.util.Optional;

public interface CouponGateway {
    Coupon create(Coupon coupon);

    Optional<Coupon> findById(CouponID id);

    Optional<Coupon> findByCode(String code);

    Optional<Coupon> claimUse(String code, Instant now);
}
