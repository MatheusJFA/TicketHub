package com.tickethub.infrastructure.coupon.persistence;

import com.tickethub.domain.core.coupon.Coupon;
import com.tickethub.domain.core.coupon.CouponID;
import com.tickethub.domain.core.coupon.CouponKind;
import com.tickethub.infrastructure.audit.AuditActor;
import com.tickethub.infrastructure.shared.persistence.MoneyDocument;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("coupons")
public record CouponDocument(
        @Id String id,
        String code,
        String showId,
        String sectionId,
        String kind,
        Integer percent,
        MoneyDocument fixed,
        Instant validFrom,
        Instant validUntil,
        Integer maxUses,
        int usedCount,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        String createdBy,
        String lastModifiedBy) {

    public static final String COLLECTION = "coupons";

    public CouponDocument withActors(final String createdBy, final String lastModifiedBy) {
        return new CouponDocument(
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

    public static CouponDocument from(final Coupon coupon) {
        return stamped(new CouponDocument(
                coupon.getId().getValue(),
                coupon.getCode(),
                coupon.getShowId(),
                coupon.getSectionId(),
                Optional.ofNullable(coupon.getKind()).map(CouponKind::name).orElse(null),
                coupon.getPercent(),
                MoneyDocument.from(coupon.getFixed()),
                coupon.getValidFrom(),
                coupon.getValidUntil(),
                coupon.getMaxUses(),
                coupon.getUsedCount(),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt(),
                coupon.getDeletedAt(),
                coupon.getCreatedBy(),
                coupon.getLastModifiedBy()));
    }

    private static CouponDocument stamped(final CouponDocument document) {
        final var actor = AuditActor.currentOrAnonymous();
        final var createdBy = Optional.ofNullable(document.createdBy()).orElse(actor);
        return document.withActors(createdBy, actor);
    }

    public Coupon toDomain() {
        return Coupon.reconstitute(
                CouponID.from(id),
                code,
                showId,
                sectionId,
                Optional.ofNullable(kind).map(CouponKind::valueOf).orElse(null),
                percent,
                Optional.ofNullable(fixed).map(MoneyDocument::toDomain).orElse(null),
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
}
