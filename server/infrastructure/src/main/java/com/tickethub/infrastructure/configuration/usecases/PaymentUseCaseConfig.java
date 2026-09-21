package com.tickethub.infrastructure.configuration.usecases;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tickethub.application.payment.confirm.ConfirmPaymentUseCase;
import com.tickethub.application.payment.confirm.DefaultConfirmPaymentUseCase;
import com.tickethub.application.payment.pay.DefaultPayOrderUseCase;
import com.tickethub.application.payment.pay.PayOrderUseCase;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.domain.core.ticket.TicketGateway;
import com.tickethub.domain.core.ticket.TicketSigner;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({OrderGateway.class, TicketGateway.class, PaymentGateway.class, TicketSigner.class})
public class PaymentUseCaseConfig {
    private final OrderGateway orderGateway;
    private final TicketGateway ticketGateway;
    private final PaymentGateway paymentGateway;
    private final TicketSigner ticketSigner;

    public PaymentUseCaseConfig(final OrderGateway orderGateway, final TicketGateway ticketGateway,
            final PaymentGateway paymentGateway, final TicketSigner ticketSigner) {
        this.orderGateway = orderGateway;
        this.ticketGateway = ticketGateway;
        this.paymentGateway = paymentGateway;
        this.ticketSigner = ticketSigner;
    }

    @Bean
    public PayOrderUseCase payOrderUseCase() {
        return new DefaultPayOrderUseCase(orderGateway, paymentGateway);
    }

    @Bean
    public ConfirmPaymentUseCase confirmPaymentUseCase() {
        return new DefaultConfirmPaymentUseCase(orderGateway, ticketGateway, ticketSigner, paymentGateway);
    }
}
