package com.tickethub.infrastructure.configuration;

import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import com.tickethub.infrastructure.web.CorrelationIdFilter;
import org.apache.commons.lang3.StringUtils;

@Configuration(proxyBeanMethods = false)
public class AuditConfiguration {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(MDC.get(CorrelationIdFilter.ACTOR_KEY))
                .filter(StringUtils::isNotBlank)
                .or(() -> Optional.of(CorrelationIdFilter.ANONYMOUS_ACTOR));
    }
}
