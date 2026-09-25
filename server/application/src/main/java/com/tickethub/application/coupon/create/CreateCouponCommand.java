package com.tickethub.application.coupon.create;

import com.tickethub.domain.core.coupon.CouponKind;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateCouponCommand(
        String code,
        String showId,
        String sectionId,
        CouponKind kind,
        Integer percent,
        BigDecimal fixedValue,
        String fixedCurrency,
        Instant validFrom,
        Instant validUntil,
        Integer maxUses) {
    public static CreateCouponCommand with(
            final String code,
            final String showId,
            final String sectionId,
            final CouponKind kind,
            final Integer percent,
            final BigDecimal fixedValue,
            final String fixedCurrency,
            final Instant validFrom,
            final Instant validUntil,
            final Integer maxUses) {
        return new CreateCouponCommand(
                code, showId, sectionId, kind, percent, fixedValue, fixedCurrency, validFrom, validUntil, maxUses);
    }
}
