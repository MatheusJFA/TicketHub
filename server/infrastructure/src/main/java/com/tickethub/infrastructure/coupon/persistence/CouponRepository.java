package com.tickethub.infrastructure.coupon.persistence;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CouponRepository extends MongoRepository<CouponDocument, String> {

    Optional<CouponDocument> findByCode(String code);
}
