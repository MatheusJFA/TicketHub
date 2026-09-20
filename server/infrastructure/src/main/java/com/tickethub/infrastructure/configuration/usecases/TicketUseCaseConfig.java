package com.tickethub.infrastructure.configuration.usecases;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tickethub.application.ticket.validate.DefaultValidateTicketUseCase;
import com.tickethub.application.ticket.validate.ValidateTicketUseCase;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.spot.SpotGateway;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({SpotGateway.class, ShowGateway.class})
public class TicketUseCaseConfig {
    private final SpotGateway spotGateway;
    private final ShowGateway showGateway;

    public TicketUseCaseConfig(final SpotGateway spotGateway, final ShowGateway showGateway) {
        this.spotGateway = spotGateway;
        this.showGateway = showGateway;
    }

    @Bean
    public ValidateTicketUseCase validateTicketUseCase() {
        return new DefaultValidateTicketUseCase(spotGateway, showGateway);
    }
}
