package com.tickethub.infrastructure.configuration.usecases;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tickethub.application.cep.lookup.DefaultLookupCepUseCase;
import com.tickethub.application.cep.lookup.LookupCepUseCase;
import com.tickethub.domain.geo.CepLookup;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({CepLookup.class})
public class CepUseCaseConfig {
    private final CepLookup cepLookup;

    public CepUseCaseConfig(final CepLookup cepLookup) {
        this.cepLookup = cepLookup;
    }

    @Bean
    public LookupCepUseCase lookupCepUseCase() {
        return new DefaultLookupCepUseCase(cepLookup);
    }
}
