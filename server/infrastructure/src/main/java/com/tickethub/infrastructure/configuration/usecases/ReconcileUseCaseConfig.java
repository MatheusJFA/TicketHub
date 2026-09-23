package com.tickethub.infrastructure.configuration.usecases;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tickethub.application.payment.reconcile.DefaultReconcileOrdersUseCase;
import com.tickethub.application.payment.reconcile.ReconcileOrdersUseCase;
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
@ConditionalOnBean({OrderGateway.class, TicketGateway.class, TicketSigner.class, PaymentGateway.class,
        SpotGateway.class, SectionGateway.class, ShowGateway.class})
public class ReconcileUseCaseConfig {
    private final OrderGateway orderGateway;
    private final TicketGateway ticketGateway;
    private final TicketSigner ticketSigner;
    private final PaymentGateway paymentGateway;
    private final SpotGateway spotGateway;
    private final SaleRecorder sales;

    public ReconcileUseCaseConfig(final OrderGateway orderGateway, final TicketGateway ticketGateway,
            final TicketSigner ticketSigner, final PaymentGateway paymentGateway,
            final SpotGateway spotGateway, final SectionGateway sectionGateway,
            final ShowGateway showGateway) {
        this.orderGateway = orderGateway;
        this.ticketGateway = ticketGateway;
        this.ticketSigner = ticketSigner;
        this.paymentGateway = paymentGateway;
        this.spotGateway = spotGateway;
        this.sales = new DefaultSaleRecorder(spotGateway, sectionGateway, showGateway);
    }

    @Bean
    public ReconcileOrdersUseCase reconcileOrdersUseCase() {
        return new DefaultReconcileOrdersUseCase(orderGateway, ticketGateway, ticketSigner,
                paymentGateway, spotGateway, sales);
    }
}
