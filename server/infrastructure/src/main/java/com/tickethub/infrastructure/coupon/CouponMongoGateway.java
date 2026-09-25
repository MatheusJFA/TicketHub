package com.tickethub.infrastructure.coupon;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.core.coupon.Coupon;
import com.tickethub.domain.core.coupon.CouponGateway;
import com.tickethub.domain.core.coupon.CouponID;
import com.tickethub.infrastructure.coupon.persistence.CouponDocument;
import com.tickethub.infrastructure.coupon.persistence.CouponRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.bson.Document;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class CouponMongoGateway implements CouponGateway {

    private final MongoTemplate mongoTemplate;
    private final CouponRepository repository;

    public CouponMongoGateway(final MongoTemplate mongoTemplate, final CouponRepository repository) {
        this.mongoTemplate = requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
        this.repository = requireNonNull(repository, "'repository' should not be null");
    }

    @Override
    public Coupon create(final Coupon coupon) {
        return mongoTemplate
                .insert(CouponDocument.from(coupon), CouponDocument.COLLECTION)
                .toDomain();
    }

    @Override
    public Optional<Coupon> findById(final CouponID id) {
        return repository.findById(id.getValue()).map(CouponDocument::toDomain);
    }

    @Override
    public Optional<Coupon> findByCode(final String code) {
        return repository.findByCode(code).map(CouponDocument::toDomain);
    }

    @Override
    public Optional<Coupon> claimUse(final String code, final Instant now) {
        // Atomic check-and-increment: the validity window and the
        // usedCount<maxUses comparison run server-side, so concurrent
        // checkouts cannot overspend maxUses. MQL null-equality matches
        // missing fields too; only the field-to-field comparison needs $expr.
        final Document filter = new Document("code", code)
                .append(
                        "$and",
                        List.of(
                                new Document(
                                        "$or",
                                        List.of(
                                                new Document("validFrom", null),
                                                new Document("validFrom", new Document("$lte", now)))),
                                new Document(
                                        "$or",
                                        List.of(
                                                new Document("validUntil", null),
                                                new Document("validUntil", new Document("$gte", now)))),
                                new Document(
                                        "$or",
                                        List.of(
                                                new Document("maxUses", null),
                                                new Document(
                                                        "$expr",
                                                        new Document("$lt", List.of("$usedCount", "$maxUses")))))));
        final Query query = new BasicQuery(filter);
        final CouponDocument claimed = mongoTemplate.findAndModify(
                query,
                new Update().inc("usedCount", 1),
                FindAndModifyOptions.options().returnNew(true),
                CouponDocument.class,
                CouponDocument.COLLECTION);
        return Optional.ofNullable(claimed).map(CouponDocument::toDomain);
    }
}
