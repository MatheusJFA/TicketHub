package com.tickethub.infrastructure.configuration.usecases;

import com.tickethub.application.order.cancel.CancelOrderUseCase;
import com.tickethub.application.order.cancel.DefaultCancelOrderUseCase;
import com.tickethub.application.order.create.CreateOrderUseCase;
import com.tickethub.application.order.create.DefaultCreateOrderUseCase;
import com.tickethub.application.order.expire.DefaultExpireOrdersUseCase;
import com.tickethub.application.order.expire.ExpireOrdersUseCase;
import com.tickethub.application.order.retrieve.get.DefaultGetOrderUseCase;
import com.tickethub.application.order.retrieve.get.GetOrderUseCase;
import com.tickethub.domain.core.coupon.CouponGateway;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.spot.SpotGateway;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({CustomerGateway.class, SpotGateway.class, SectionGateway.class, OrderGateway.class})
public class OrderUseCaseConfig {
    private final CustomerGateway customerGateway;
    private final SpotGateway spotGateway;
    private final SectionGateway sectionGateway;
    private final OrderGateway orderGateway;
    private final Duration reservationTtl;
    private final CouponGateway couponGateway;

    public OrderUseCaseConfig(
            final CustomerGateway customerGateway,
            final SpotGateway spotGateway,
            final SectionGateway sectionGateway,
            final OrderGateway orderGateway,
            @Value("${tickethub.orders.reservation-ttl:15m}") final Duration reservationTtl,
            final ObjectProvider<CouponGateway> coupons) {
        this.customerGateway = customerGateway;
        this.spotGateway = spotGateway;
        this.sectionGateway = sectionGateway;
        this.orderGateway = orderGateway;
        this.reservationTtl = reservationTtl;
        this.couponGateway = coupons.getIfAvailable();
    }

    @Bean
    public CreateOrderUseCase createOrderUseCase() {
        return new DefaultCreateOrderUseCase(
                customerGateway,
                spotGateway,
                sectionGateway,
                orderGateway,
                couponGateway,
                reservationTtl,
                Clock.systemUTC());
    }

    @Bean
    public CancelOrderUseCase cancelOrderUseCase() {
        return new DefaultCancelOrderUseCase(orderGateway, spotGateway);
    }

    @Bean
    public ExpireOrdersUseCase expireOrdersUseCase() {
        return new DefaultExpireOrdersUseCase(orderGateway, spotGateway);
    }

    @Bean
    public GetOrderUseCase getOrderUseCase() {
        return new DefaultGetOrderUseCase(orderGateway);
    }
}
