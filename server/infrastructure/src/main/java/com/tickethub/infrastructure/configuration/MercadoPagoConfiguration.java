package com.tickethub.infrastructure.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.infrastructure.payment.mercadopago.MercadoPagoClient;
import com.tickethub.infrastructure.payment.mercadopago.MercadoPagoPaymentGateway;
import com.tickethub.infrastructure.payment.mercadopago.MercadoPagoProperties;
import com.tickethub.infrastructure.shared.http.BaseHttpClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MercadoPagoProperties.class)
public class MercadoPagoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "tickethub.payment.mercadopago", name = "access-token")
    public MercadoPagoClient mercadoPagoClient(final RestClient.Builder builder,
            final MercadoPagoProperties properties) {
        if (properties.getAccessToken() == null || properties.getAccessToken().isBlank()) {
            throw new IllegalStateException(
                    "Mercado Pago access token is blank: set MERCADOPAGO_ACCESS_TOKEN");
        }
        final var prepared = BaseHttpClient.preparedBuilder(builder, properties.getBaseUrl());
        prepared.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAccessToken());
        prepared.requestFactory(BaseHttpClient.timedFactory(properties.getConnectTimeout(),
                properties.getReadTimeout()));
        return new MercadoPagoClient(prepared.build());
    }

    @Bean
    @ConditionalOnBean({MercadoPagoClient.class, OrderGateway.class, CustomerGateway.class})
    public PaymentGateway mercadoPagoPaymentGateway(final MercadoPagoClient client,
            final MercadoPagoProperties properties, final OrderGateway orderGateway,
            final CustomerGateway customerGateway) {
        return new MercadoPagoPaymentGateway(client, properties, orderGateway, customerGateway);
    }
}
