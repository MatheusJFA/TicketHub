package com.tickethub.infrastructure.configuration.usecases;

import com.tickethub.application.ticket.retrieve.bycustomer.DefaultListCustomerTicketsUseCase;
import com.tickethub.application.ticket.retrieve.bycustomer.ListCustomerTicketsUseCase;
import com.tickethub.application.ticket.retrieve.byshow.DefaultListShowTicketsUseCase;
import com.tickethub.application.ticket.retrieve.byshow.ListShowTicketsUseCase;
import com.tickethub.application.ticket.validate.DefaultValidateTicketUseCase;
import com.tickethub.application.ticket.validate.ValidateTicketUseCase;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.ticket.TicketGateway;
import com.tickethub.domain.core.ticket.TicketSigner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({TicketGateway.class, TicketSigner.class, SpotGateway.class, ShowGateway.class})
public class TicketUseCaseConfig {
    private final TicketGateway ticketGateway;
    private final TicketSigner ticketSigner;
    private final SpotGateway spotGateway;
    private final ShowGateway showGateway;

    public TicketUseCaseConfig(
            final TicketGateway ticketGateway,
            final TicketSigner ticketSigner,
            final SpotGateway spotGateway,
            final ShowGateway showGateway) {
        this.ticketGateway = ticketGateway;
        this.ticketSigner = ticketSigner;
        this.spotGateway = spotGateway;
        this.showGateway = showGateway;
    }

    @Bean
    public ValidateTicketUseCase validateTicketUseCase() {
        return new DefaultValidateTicketUseCase(ticketGateway, ticketSigner, spotGateway, showGateway);
    }

    @Bean
    public ListCustomerTicketsUseCase listCustomerTicketsUseCase() {
        return new DefaultListCustomerTicketsUseCase(ticketGateway, spotGateway, showGateway);
    }

    @Bean
    public ListShowTicketsUseCase listShowTicketsUseCase() {
        return new DefaultListShowTicketsUseCase(ticketGateway, spotGateway);
    }
}
