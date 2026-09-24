package com.tickethub.infrastructure.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.infrastructure.payment.DisabledPaymentGateway;
import com.tickethub.infrastructure.payment.mercadopago.MercadoPagoClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

@DisplayName("Mercado Pago configuration")
class MercadoPagoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(MercadoPagoConfiguration.class, PaymentFallbackConfiguration.class)
            .withBean(RestClient.Builder.class, RestClient::builder);

    @Test
    @DisplayName("Without token, boots healthy with disabled gateway")
    void withoutToken_bootsHealthyWithDisabledGateway() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(MercadoPagoClient.class);
            assertThat(context).hasSingleBean(PaymentGateway.class);
            assertThat(context.getBean(PaymentGateway.class)).isInstanceOf(DisabledPaymentGateway.class);
        });
    }

    @Test
    @DisplayName("With blank token and gateways, still exposes disabled gateway")
    void withBlankTokenAndGateways_exposesDisabledGateway() {
        runner.withBean(OrderGateway.class, () -> mock(OrderGateway.class))
                .withBean(CustomerGateway.class, () -> mock(CustomerGateway.class))
                .withPropertyValues("tickethub.payment.mercadopago.access-token=")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(MercadoPagoClient.class);
                    assertThat(context).getBean(PaymentGateway.class)
                            .isInstanceOf(DisabledPaymentGateway.class);
                });
    }

    @Test
    @DisplayName("With token, wires real client and skips fallback")
    void withToken_wiresRealClientAndSkipsFallback() {
        runner.withBean(OrderGateway.class, () -> mock(OrderGateway.class))
                .withBean(CustomerGateway.class, () -> mock(CustomerGateway.class))
                .withPropertyValues("tickethub.payment.mercadopago.access-token=test-access-token")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(MercadoPagoClient.class);
                    assertThat(context).getBean(PaymentGateway.class).isNotInstanceOf(DisabledPaymentGateway.class);
                });
    }

    @Test
    @DisplayName("With token but disabled flag, keeps disabled gateway")
    void withTokenButDisabled_keepsDisabledGateway() {
        runner.withPropertyValues(
                        "tickethub.payment.mercadopago.enabled=false",
                        "tickethub.payment.mercadopago.access-token=test-access-token")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(MercadoPagoClient.class);
                    assertThat(context).getBean(PaymentGateway.class)
                            .isInstanceOf(DisabledPaymentGateway.class);
                });
    }
}
