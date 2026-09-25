package com.tickethub.domain.core.coupon;

import com.tickethub.domain.AggregateRoot;
import com.tickethub.domain.shared.Money;
import com.tickethub.domain.validation.ValidationHandler;
import java.time.Instant;

public class Coupon extends AggregateRoot<CouponID> {
    private String code;
    private String showId;
    private String sectionId;
    private CouponKind kind;
    private Integer percent;
    private Money fixed;
    private Instant validFrom;
    private Instant validUntil;
    private Integer maxUses;
    private int usedCount;

    private Coupon(
            CouponID id,
            String code,
            String showId,
            String sectionId,
            CouponKind kind,
            Integer percent,
            Money fixed,
            Instant validFrom,
            Instant validUntil,
            Integer maxUses,
            int usedCount,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt,
            String createdBy,
            String lastModifiedBy) {
        super(id, createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
        this.code = code;
        this.showId = showId;
        this.sectionId = sectionId;
        this.kind = kind;
        this.percent = percent;
        this.fixed = fixed;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.maxUses = maxUses;
        this.usedCount = usedCount;
    }

    public static String normalizeCode(final String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    public static Coupon create(
            String code,
            String showId,
            String sectionId,
            CouponKind kind,
            Integer percent,
            Money fixed,
            Instant validFrom,
            Instant validUntil,
            Integer maxUses) {
        final CouponID id = CouponID.generate();
        final var now = Instant.now();
        return new Coupon(
                id,
                normalizeCode(code),
                showId,
                sectionId,
                kind,
                percent,
                fixed,
                validFrom,
                validUntil,
                maxUses,
                0,
                now,
                now,
                null,
                null,
                null);
    }

    public static Coupon reconstitute(
            CouponID id,
            String code,
            String showId,
            String sectionId,
            CouponKind kind,
            Integer percent,
            Money fixed,
            Instant validFrom,
            Instant validUntil,
            Integer maxUses,
            int usedCount,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt,
            String createdBy,
            String lastModifiedBy) {
        return new Coupon(
                id,
                code,
                showId,
                sectionId,
                kind,
                percent,
                fixed,
                validFrom,
                validUntil,
                maxUses,
                usedCount,
                createdAt,
                updatedAt,
                deletedAt,
                createdBy,
                lastModifiedBy);
    }

    public boolean isActiveAt(final Instant now) {
        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }
        if (validUntil != null && now.isAfter(validUntil)) {
            return false;
        }
        return maxUses == null || usedCount < maxUses;
    }

    public boolean appliesTo(final String itemShowId, final String itemSectionId) {
        if (sectionId != null) {
            return sectionId.equals(itemSectionId);
        }
        if (showId != null) {
            return showId.equals(itemShowId);
        }
        return false;
    }

    public String getCode() {
        return code;
    }

    public String getShowId() {
        return showId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public CouponKind getKind() {
        return kind;
    }

    public Integer getPercent() {
        return percent;
    }

    public Money getFixed() {
        return fixed;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public int getUsedCount() {
        return usedCount;
    }

    @Override
    public void validate(final ValidationHandler handler) {
        final var validator = new CouponValidator(this, handler);
        validator.validate();
    }
}
