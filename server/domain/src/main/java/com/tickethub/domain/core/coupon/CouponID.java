package com.tickethub.domain.core.coupon;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.Identifier;
import java.util.UUID;

public class CouponID extends Identifier {
    private final String value;

    private CouponID(String value) {
        this.value = requireNonNull(value, "'CouponID' should not be null");
    }

    public static CouponID generate() {
        UUID uuid = UUID.randomUUID();
        return new CouponID(uuid.toString());
    }

    public static CouponID from(String value) {
        return new CouponID(value);
    }

    @Override
    public String getValue() {
        return value;
    }
}
