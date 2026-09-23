package com.tickethub.infrastructure.configuration.usecases;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tickethub.application.payment.confirm.ConfirmPaymentUseCase;
import com.tickethub.application.payment.confirm.DefaultConfirmPaymentUseCase;
import com.tickethub.application.payment.pay.DefaultPayOrderUseCase;
import com.tickethub.application.payment.pay.PayOrderUseCase;
import com.tickethub.application.sales.DefaultSaleRecorder;
import com.tickethub.application.sales.SaleRecorder;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.ticket.TicketGateway;
import com.tickethub.domain.core.ticket.TicketSigner;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({OrderGateway.class, TicketGateway.class, PaymentGateway.class, TicketSigner.class,
        SpotGateway.class, SectionGateway.class, ShowGateway.class})
public class PaymentUseCaseConfig {
    private final OrderGateway orderGateway;
    private final TicketGateway ticketGateway;
    private final PaymentGateway paymentGateway;
    private final TicketSigner ticketSigner;
    private final SaleRecorder sales;

    public PaymentUseCaseConfig(final OrderGateway orderGateway, final TicketGateway ticketGateway,
            final PaymentGateway paymentGateway, final TicketSigner ticketSigner,
            final SpotGateway spotGateway, final SectionGateway sectionGateway,
            final ShowGateway showGateway) {
        this.orderGateway = orderGateway;
        this.ticketGateway = ticketGateway;
        this.paymentGateway = paymentGateway;
        this.ticketSigner = ticketSigner;
        this.sales = new DefaultSaleRecorder(spotGateway, sectionGateway, showGateway);
    }

    @Bean
    public PayOrderUseCase payOrderUseCase() {
        return new DefaultPayOrderUseCase(orderGateway, paymentGateway);
    }

    @Bean
    public ConfirmPaymentUseCase confirmPaymentUseCase() {
        return new DefaultConfirmPaymentUseCase(orderGateway, ticketGateway, ticketSigner, paymentGateway,
                sales);
    }
}
