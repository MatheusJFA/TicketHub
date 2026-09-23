package com.tickethub.infrastructure.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Optional;

import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.application.payment.confirm.ConfirmPaymentUseCase;
import com.tickethub.infrastructure.payment.mercadopago.MercadoPagoClient;
import com.tickethub.infrastructure.payment.mercadopago.MercadoPagoPaymentGateway;
import com.tickethub.infrastructure.payment.mercadopago.MercadoPagoProperties;
import com.tickethub.infrastructure.payment.mercadopago.MercadoPagoWebhookHandler;
import com.tickethub.infrastructure.payment.mercadopago.MercadoPagoWebhookVerifier;
import com.tickethub.infrastructure.shared.http.BaseHttpClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MercadoPagoProperties.class)
public class MercadoPagoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoConfiguration.class);

    @Bean
    @ConditionalOnProperty(prefix = "tickethub.payment.mercadopago", name = "access-token")
    public MercadoPagoClient mercadoPagoClient(final RestClient.Builder builder,
            final MercadoPagoProperties properties) {
        if (isBlank(properties.getAccessToken())) {
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

    @Bean
    @ConditionalOnBean({MercadoPagoClient.class, PaymentGateway.class, ConfirmPaymentUseCase.class})
    public MercadoPagoWebhookVerifier mercadoPagoWebhookVerifier(final MercadoPagoProperties properties) {
        if (isBlank(properties.getWebhookSecret())) {
            log.warn("Mercado Pago webhook secret is blank: POST /payments/mercadopago will answer 401");
        }
        return new MercadoPagoWebhookVerifier(
                Optional.ofNullable(properties.getWebhookSecret()).orElse(""),
                properties.getWebhookTolerance());
    }

    @Bean
    @ConditionalOnBean({MercadoPagoWebhookVerifier.class, PaymentGateway.class, ConfirmPaymentUseCase.class})
    public MercadoPagoWebhookHandler mercadoPagoWebhookHandler(final MercadoPagoWebhookVerifier verifier,
            final PaymentGateway paymentGateway, final ConfirmPaymentUseCase confirmPayment) {
        return new MercadoPagoWebhookHandler(verifier, paymentGateway, confirmPayment);
    }
}
