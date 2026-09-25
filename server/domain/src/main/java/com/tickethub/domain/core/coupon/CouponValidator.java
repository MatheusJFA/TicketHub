package com.tickethub.domain.core.coupon;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.ValidationHandler;
import com.tickethub.domain.validation.Validator;
import java.util.regex.Pattern;

public final class CouponValidator extends Validator {
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9][A-Z0-9_-]{3,31}$");

    private final Coupon coupon;

    public CouponValidator(final Coupon coupon, final ValidationHandler handler) {
        super(handler);
        this.coupon = coupon;
    }

    @Override
    public void validate() {
        final ValidationHandler handler = validationHandler();

        if (isBlank(coupon.getCode()) || !CODE_PATTERN.matcher(coupon.getCode()).matches()) {
            handler.append(new Error("'code' should match [A-Z0-9_-]{4,32}"));
        }
        if (coupon.getKind() == null) {
            handler.append(new Error("'kind' should not be null"));
        }
        final boolean hasShow = isNotBlank(coupon.getShowId());
        final boolean hasSection = isNotBlank(coupon.getSectionId());
        if (hasShow == hasSection) {
            handler.append(new Error("coupon must target either a show or a section"));
        }
        if (coupon.getKind() == CouponKind.PERCENT) {
            if (coupon.getPercent() == null || coupon.getPercent() < 1 || coupon.getPercent() > 100) {
                handler.append(new Error("'percent' should be between 1 and 100"));
            }
            if (coupon.getFixed() != null) {
                handler.append(new Error("'fixed' must be null for percent coupons"));
            }
        }
        if (coupon.getKind() == CouponKind.FIXED) {
            if (isNull(coupon.getFixed())) {
                handler.append(new Error("'fixed' should not be null for fixed coupons"));
            }
            if (coupon.getPercent() != null) {
                handler.append(new Error("'percent' must be null for fixed coupons"));
            }
        }
        if (coupon.getValidFrom() != null
                && coupon.getValidUntil() != null
                && !coupon.getValidFrom().isBefore(coupon.getValidUntil())) {
            handler.append(new Error("'validFrom' should be before 'validUntil'"));
        }
        if (coupon.getMaxUses() != null && coupon.getMaxUses() < 1) {
            handler.append(new Error("'maxUses' should be at least 1"));
        }
        if (coupon.getUsedCount() < 0) {
            handler.append(new Error("'usedCount' should not be negative"));
        }
    }
}
