package com.tickethub.infrastructure.configuration;

import com.tickethub.infrastructure.web.CorrelationIdFilter;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

@Configuration(proxyBeanMethods = false)
public class AuditConfiguration {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(MDC.get(CorrelationIdFilter.ACTOR_KEY))
                .filter(StringUtils::isNotBlank)
                .or(() -> Optional.of(CorrelationIdFilter.ANONYMOUS_ACTOR));
    }
}
