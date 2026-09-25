package com.tickethub.infrastructure.coupon.presenters;

import com.tickethub.application.coupon.create.CreateCouponCommand;
import com.tickethub.infrastructure.coupon.models.CreateCouponRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CouponMapper {

    CreateCouponCommand toCommand(CreateCouponRequest request);
}
