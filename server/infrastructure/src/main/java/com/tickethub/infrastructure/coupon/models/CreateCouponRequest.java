package com.tickethub.infrastructure.coupon.models;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateCouponRequest(
        String code,
        String showId,
        String sectionId,
        String kind,
        Integer percent,
        BigDecimal fixedValue,
        String fixedCurrency,
        Instant validFrom,
        Instant validUntil,
        Integer maxUses) {}
