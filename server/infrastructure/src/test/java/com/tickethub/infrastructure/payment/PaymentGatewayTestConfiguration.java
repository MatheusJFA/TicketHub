package com.tickethub.infrastructure.payment;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.tickethub.domain.core.payment.PaymentGateway;

/**
 * Exposes the in-memory payment gateway to full-context suites, which boot
 * without Mercado Pago credentials. Production stays fail-fast without a
 * token; slices and unit tests are unaffected (they never import this).
 */
@TestConfiguration
public class PaymentGatewayTestConfiguration {

    @Bean
    @Primary
    public PaymentGateway paymentGateway() {
        return new InMemoryPaymentGateway();
    }
}
