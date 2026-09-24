package com.tickethub.infrastructure.payment;

import com.tickethub.domain.core.payment.PaymentGateway;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Exposes the in-memory payment gateway to full-context suites, which boot
 * without Mercado Pago credentials. Without a token production wires the
 * disabled gateway (pay paths answer 503); slices and unit tests are
 * unaffected (they never import this).
 */
@TestConfiguration
public class PaymentGatewayTestConfiguration {

    @Bean
    @Primary
    public PaymentGateway paymentGateway() {
        return new InMemoryPaymentGateway();
    }
}
