package com.tickethub.infrastructure.configuration.usecases;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tickethub.application.zipcode.lookup.DefaultLookupZipCodeUseCase;
import com.tickethub.application.zipcode.lookup.LookupZipCodeUseCase;
import com.tickethub.domain.geography.ZipCodeLookup;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({ZipCodeLookup.class})
public class ZipCodeUseCaseConfig {
    private final ZipCodeLookup zipCodeLookup;

    public ZipCodeUseCaseConfig(final ZipCodeLookup zipCodeLookup) {
        this.zipCodeLookup = zipCodeLookup;
    }

    @Bean
    public LookupZipCodeUseCase lookupZipCodeUseCase() {
        return new DefaultLookupZipCodeUseCase(zipCodeLookup);
    }
}
