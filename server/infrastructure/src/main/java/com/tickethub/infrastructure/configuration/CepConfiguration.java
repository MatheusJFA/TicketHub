package com.tickethub.infrastructure.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.tickethub.infrastructure.cep.CepProperties;
import com.tickethub.infrastructure.cep.ViaCepClient;
import com.tickethub.infrastructure.shared.http.BaseHttpClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CepProperties.class)
public class CepConfiguration {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public ViaCepClient viaCepClient(final RestClient.Builder builder, final CepProperties properties) {
        final var prepared = BaseHttpClient.preparedBuilder(builder, properties.getBaseUrl());
        prepared.requestFactory(BaseHttpClient.timedFactory(properties.getConnectTimeout(),
                properties.getReadTimeout()));
        return new ViaCepClient(prepared.build());
    }
}
