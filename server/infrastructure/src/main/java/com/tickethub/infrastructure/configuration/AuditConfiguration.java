package com.tickethub.infrastructure.configuration;

import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import com.tickethub.infrastructure.api.ApiSupport;
import com.tickethub.infrastructure.audit.AuditTrail;
import com.tickethub.infrastructure.web.CorrelationIdFilter;

@Configuration(proxyBeanMethods = false)
public class AuditConfiguration {

    public AuditConfiguration(final ObjectProvider<AuditTrail> trails) {
        final AuditTrail trail = trails.getIfAvailable();
        ApiSupport.configureAuditTrail(trail == null ? AuditTrail.noop() : trail);
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(MDC.get(CorrelationIdFilter.ACTOR_KEY))
                .filter(actor -> !actor.isBlank())
                .or(() -> Optional.of(CorrelationIdFilter.ANONYMOUS_ACTOR));
    }
}
