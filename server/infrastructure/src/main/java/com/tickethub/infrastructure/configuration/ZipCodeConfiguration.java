package com.tickethub.infrastructure.configuration;

import com.tickethub.infrastructure.shared.http.BaseHttpClient;
import com.tickethub.infrastructure.zipcode.ViaCepClient;
import com.tickethub.infrastructure.zipcode.ZipCodeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ZipCodeProperties.class)
public class ZipCodeConfiguration {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public ViaCepClient viaCepClient(final RestClient.Builder builder, final ZipCodeProperties properties) {
        final var prepared = BaseHttpClient.preparedBuilder(builder, properties.getBaseUrl());
        prepared.requestFactory(
                BaseHttpClient.timedFactory(properties.getConnectTimeout(), properties.getReadTimeout()));
        return new ViaCepClient(prepared.build());
    }
}
