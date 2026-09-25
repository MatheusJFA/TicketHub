package com.tickethub.infrastructure.coupon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tickethub.domain.core.coupon.Coupon;
import com.tickethub.domain.core.coupon.CouponKind;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;
import com.tickethub.infrastructure.coupon.persistence.CouponDocument;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@IntegrationTest
@DisplayName("Coupon Mongo gateway")
class CouponMongoGatewayIT extends ContainerSupport {

    @Autowired
    private CouponMongoGateway gateway;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanUp() {
        mongoTemplate.remove(new Query(), CouponDocument.COLLECTION);
    }

    @Test
    @DisplayName("Given coupon, when create and find, then roundtrips")
    void givenCoupon_whenCreateAndFind_thenRoundtrips() {
        final var coupon = Coupon.create("PISTA10", null, "section-1", CouponKind.PERCENT, 10, null, null, null, 5);

        gateway.create(coupon);

        final var found = gateway.findByCode("PISTA10").orElseThrow();
        assertEquals(coupon.getId().getValue(), found.getId().getValue());
        assertEquals(0, found.getUsedCount());
    }

    @Test
    @DisplayName("Given uses left, when claim, then increments atomically")
    void givenUsesLeft_whenClaim_thenIncrementsAtomically() {
        final var coupon = Coupon.create("ONEUSE", null, "section-1", CouponKind.PERCENT, 10, null, null, null, 1);
        gateway.create(coupon);

        final var claimed = gateway.claimUse("ONEUSE", Instant.now()).orElseThrow();
        assertEquals(1, claimed.getUsedCount());
        assertTrue(gateway.claimUse("ONEUSE", Instant.now()).isEmpty());
    }

    @Test
    @DisplayName("Given unlimited coupon, when claim, then succeeds repeatedly")
    void givenUnlimitedCoupon_whenClaim_thenSucceedsRepeatedly() {
        final var coupon = Coupon.create("FREE", null, "section-1", CouponKind.PERCENT, 10, null, null, null, null);
        gateway.create(coupon);

        assertTrue(gateway.claimUse("FREE", Instant.now()).isPresent());
        assertTrue(gateway.claimUse("FREE", Instant.now()).isPresent());
    }

    @Test
    @DisplayName("Given expired coupon, when claim, then returns empty")
    void givenExpiredCoupon_whenClaim_thenReturnsEmpty() {
        final var coupon = Coupon.create(
                "OLD",
                null,
                "section-1",
                CouponKind.PERCENT,
                10,
                null,
                Instant.now().minusSeconds(120),
                Instant.now().minusSeconds(60),
                null);
        gateway.create(coupon);

        assertTrue(gateway.claimUse("OLD", Instant.now()).isEmpty());
    }

    @Test
    @DisplayName("Given unknown code, when claim, then returns empty")
    void givenUnknownCode_whenClaim_thenReturnsEmpty() {
        assertTrue(gateway.claimUse("GHOST", Instant.now()).isEmpty());
    }
}
