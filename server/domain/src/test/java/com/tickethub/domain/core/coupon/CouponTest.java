package com.tickethub.domain.core.coupon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tickethub.domain.shared.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Coupon")
class CouponTest {

    private static final Currency BRL = Currency.getInstance("BRL");

    private static Coupon percent() {
        return Coupon.create("PISTA10", null, "section-1", CouponKind.PERCENT, 10, null, null, null, null);
    }

    @Test
    @DisplayName("Given valid params, when create, then normalizes code and starts pending use")
    void givenValidParams_whenCreate_thenNormalizesCode() {
        final var coupon = Coupon.create("  pista10 ", "show-1", null, CouponKind.PERCENT, 10, null, null, null, 5);

        assertNotNull(coupon.getId());
        assertEquals("PISTA10", coupon.getCode());
        assertEquals(0, coupon.getUsedCount());
        assertTrue(coupon.isActiveAt(Instant.now()));
        assertTrue(coupon.appliesTo("show-1", "section-9"));
        assertFalse(coupon.appliesTo("show-2", "section-9"));
    }

    @Test
    @DisplayName("Given section coupon, when applies, then matches section only")
    void givenSectionCoupon_whenApplies_thenMatchesSectionOnly() {
        final var coupon = percent();

        assertTrue(coupon.appliesTo("show-9", "section-1"));
        assertFalse(coupon.appliesTo("show-9", "section-2"));
    }

    @Test
    @DisplayName("Given validity window, when check activity, then respects bounds")
    void givenValidityWindow_whenCheckActivity_thenRespectsBounds() {
        final var now = Instant.now();
        final var coupon = Coupon.create(
                "WEEK", "show-1", null, CouponKind.PERCENT, 10, null, now.minusSeconds(60), now.plusSeconds(60), null);

        assertTrue(coupon.isActiveAt(now));
        assertFalse(coupon.isActiveAt(now.minusSeconds(120)));
        assertFalse(coupon.isActiveAt(now.plusSeconds(120)));
    }

    @Test
    @DisplayName("Given exhausted uses, when check activity, then inactive")
    void givenExhaustedUses_whenCheckActivity_thenInactive() {
        final var coupon = Coupon.create("ONE", "show-1", null, CouponKind.PERCENT, 10, null, null, null, 1);

        assertTrue(coupon.isActiveAt(Instant.now()));
        final var claimed = Coupon.reconstitute(
                coupon.getId(),
                coupon.getCode(),
                coupon.getShowId(),
                coupon.getSectionId(),
                coupon.getKind(),
                coupon.getPercent(),
                coupon.getFixed(),
                coupon.getValidFrom(),
                coupon.getValidUntil(),
                coupon.getMaxUses(),
                1,
                coupon.getCreatedAt(),
                coupon.getUpdatedAt(),
                coupon.getDeletedAt(),
                coupon.getCreatedBy(),
                coupon.getLastModifiedBy());

        assertFalse(claimed.isActiveAt(Instant.now()));
    }

    @Test
    @DisplayName("Given fixed coupon, when read, then exposes amount")
    void givenFixedCoupon_whenRead_thenExposesAmount() {
        final var coupon = Coupon.create(
                "FIX30",
                null,
                "section-1",
                CouponKind.FIXED,
                null,
                Money.create(new BigDecimal("30.00"), BRL),
                null,
                null,
                null);

        assertEquals(CouponKind.FIXED, coupon.getKind());
        assertEquals(new BigDecimal("30.00"), coupon.getFixed().getValue());
    }
}
