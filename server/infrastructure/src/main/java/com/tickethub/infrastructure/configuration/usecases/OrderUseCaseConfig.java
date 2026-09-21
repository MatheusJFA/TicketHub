package com.tickethub.infrastructure.configuration.usecases;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tickethub.application.order.cancel.CancelOrderUseCase;
import com.tickethub.application.order.cancel.DefaultCancelOrderUseCase;
import com.tickethub.application.order.create.CreateOrderUseCase;
import com.tickethub.application.order.create.DefaultCreateOrderUseCase;
import com.tickethub.application.order.expire.DefaultExpireOrdersUseCase;
import com.tickethub.application.order.expire.ExpireOrdersUseCase;
import com.tickethub.application.order.pay.DefaultPayOrderUseCase;
import com.tickethub.application.order.pay.PayOrderUseCase;
import com.tickethub.application.order.retrieve.get.DefaultGetOrderUseCase;
import com.tickethub.application.order.retrieve.get.GetOrderUseCase;
import com.tickethub.application.payment.confirm.ConfirmPaymentUseCase;
import com.tickethub.application.payment.confirm.DefaultConfirmPaymentUseCase;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.ticket.TicketGateway;
import com.tickethub.domain.core.ticket.TicketSigner;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({CustomerGateway.class, SpotGateway.class, SectionGateway.class,
        OrderGateway.class, TicketGateway.class, PaymentGateway.class, TicketSigner.class})
public class OrderUseCaseConfig {
    private final CustomerGateway customerGateway;
    private final SpotGateway spotGateway;
    private final SectionGateway sectionGateway;
    private final OrderGateway orderGateway;
    private final TicketGateway ticketGateway;
    private final PaymentGateway paymentGateway;
    private final TicketSigner ticketSigner;
    private final Duration reservationTtl;

    public OrderUseCaseConfig(final CustomerGateway customerGateway, final SpotGateway spotGateway,
            final SectionGateway sectionGateway, final OrderGateway orderGateway,
            final TicketGateway ticketGateway, final PaymentGateway paymentGateway,
            final TicketSigner ticketSigner,
            @Value("${tickethub.orders.reservation-ttl:15m}") final Duration reservationTtl) {
        this.customerGateway = customerGateway;
        this.spotGateway = spotGateway;
        this.sectionGateway = sectionGateway;
        this.orderGateway = orderGateway;
        this.ticketGateway = ticketGateway;
        this.paymentGateway = paymentGateway;
        this.ticketSigner = ticketSigner;
        this.reservationTtl = reservationTtl;
    }

    @Bean
    public CreateOrderUseCase createOrderUseCase() {
        return new DefaultCreateOrderUseCase(customerGateway, spotGateway, sectionGateway,
                orderGateway, reservationTtl);
    }

    @Bean
    public PayOrderUseCase payOrderUseCase() {
        return new DefaultPayOrderUseCase(orderGateway, paymentGateway);
    }

    @Bean
    public ConfirmPaymentUseCase confirmPaymentUseCase() {
        return new DefaultConfirmPaymentUseCase(orderGateway, ticketGateway, ticketSigner);
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
