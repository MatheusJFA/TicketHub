package com.tickethub.infrastructure.configuration;

import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.infrastructure.payment.DisabledPaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes a disabled {@link PaymentGateway} when the real provider is not
 * configured, so use cases and controllers still wire and the application
 * boots healthy. Pay/reconcile calls fail closed with 503.
 */
@Configuration(proxyBeanMethods = false)
public class PaymentFallbackConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PaymentFallbackConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(PaymentGateway.class)
    public PaymentGateway disabledPaymentGateway() {
        log.warn("No PaymentGateway configured (blank MERCADOPAGO_ACCESS_TOKEN): pay paths answer 503");
        return new DisabledPaymentGateway();
    }
}
