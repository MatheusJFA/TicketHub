package com.tickethub.infrastructure.api.controllers;

import com.tickethub.application.coupon.create.CreateCouponUseCase;
import com.tickethub.infrastructure.api.CouponAPI;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.api.models.IdResponse;
import com.tickethub.infrastructure.coupon.models.CreateCouponRequest;
import com.tickethub.infrastructure.coupon.presenters.CouponMapper;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CouponController implements CouponAPI {
    private final CreateCouponUseCase createCoupon;
    private final CouponMapper mapper;

    public CouponController(final CreateCouponUseCase createCoupon, final CouponMapper mapper) {
        this.createCoupon = createCoupon;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<IdResponse> createCoupon(CreateCouponRequest input) {
        final var output = HttpResults.require(createCoupon.execute(mapper.toCommand(input)));
        return ResponseEntity.created(URI.create("/coupons/" + output.id())).body(new IdResponse(output.id()));
    }
}
