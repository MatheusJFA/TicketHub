package com.tickethub.infrastructure.configuration.usecases;

import com.tickethub.application.coupon.create.CreateCouponUseCase;
import com.tickethub.application.coupon.create.DefaultCreateCouponUseCase;
import com.tickethub.domain.core.coupon.CouponGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({CouponGateway.class})
public class CouponUseCaseConfig {

    @Bean
    public CreateCouponUseCase createCouponUseCase(final CouponGateway couponGateway) {
        return new DefaultCreateCouponUseCase(couponGateway);
    }
}
