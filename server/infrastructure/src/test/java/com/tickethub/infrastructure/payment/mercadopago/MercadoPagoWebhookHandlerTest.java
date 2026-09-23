package com.tickethub.infrastructure.payment.mercadopago;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickethub.application.Either;
import com.tickethub.application.payment.confirm.ConfirmPaymentOutput;
import com.tickethub.application.payment.confirm.ConfirmPaymentUseCase;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.payment.Charge;
import com.tickethub.domain.core.payment.ChargeID;
import com.tickethub.domain.core.payment.ChargeStatus;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.domain.exception.ResourceNotFoundException;
import com.tickethub.domain.shared.Money;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Mercado Pago webhook handler")
class MercadoPagoWebhookHandlerTest {

    private static final String SECRET = "test_secret_key";
    private static final String DATA_ID = "123456789";
    private static final String REQUEST_ID = "req-abc";
    private static final String TS = "1704908010";
    private static final String V1 = "d600632a9073f1582726cd2aa120bb6b4f82f1d906f24cc3a627769e0cdd4b5c";

    private record Fixture(MercadoPagoWebhookHandler handler, PaymentGateway gateway, ConfirmPaymentUseCase useCase) {}

    private Fixture fixture() {
        final var clock = Clock.fixed(Instant.ofEpochSecond(1704908010), ZoneOffset.UTC);
        final var verifier = new MercadoPagoWebhookVerifier(SECRET, Duration.ofMinutes(5), clock);
        final var gateway = mock(PaymentGateway.class);
        final var useCase = mock(ConfirmPaymentUseCase.class);
        return new Fixture(new MercadoPagoWebhookHandler(verifier, gateway, useCase), gateway, useCase);
    }

    private Charge paidCharge() {
        final var charge = Charge.create(
                ChargeID.from(DATA_ID),
                OrderID.from("order-1"),
                Money.create(new java.math.BigDecimal("50.00"), java.util.Currency.getInstance("BRL")),
                ChargeStatus.PENDING,
                null);
        charge.markAsPaid();
        return charge;
    }

    @Test
    @DisplayName("Given verified payment, when handle, then confirms with status from API")
    void givenVerifiedPayment_whenHandle_thenConfirmsWithApiStatus() {
        final var fixture = fixture();
        when(fixture.gateway().findStatus(ChargeID.from(DATA_ID))).thenReturn(paidCharge());
        when(fixture.useCase().execute(any())).thenReturn(Either.right(new ConfirmPaymentOutput("order-1", "PAID")));

        final var result = fixture.handler().handle("ts=" + TS + ",v1=" + V1, REQUEST_ID, DATA_ID, "payment");

        assertThat(result).isPresent();
        assertThat(result.get().isRight()).isTrue();
        verify(fixture.useCase())
                .execute(argThat(command ->
                        command.chargeId().equals(DATA_ID) && command.status().equals("PAID")));
    }

    private static <T> T argThat(final java.util.function.Predicate<T> predicate) {
        return org.mockito.ArgumentMatchers.argThat(predicate::test);
    }

    @Test
    @DisplayName("Given bad signature, when handle, then rejects before fetching")
    void givenBadSignature_whenHandle_thenRejectsBeforeFetching() {
        final var fixture = fixture();

        assertThrows(
                InvalidWebhookSignatureException.class,
                () -> fixture.handler().handle("ts=" + TS + ",v1=" + "0".repeat(64), REQUEST_ID, DATA_ID, "payment"));
        verify(fixture.gateway(), org.mockito.Mockito.never()).findStatus(any());
    }

    @Test
    @DisplayName("Given unknown payment, when handle, then returns not-found notification")
    void givenUnknownPayment_whenHandle_thenReturnsNotFound() {
        final var fixture = fixture();
        when(fixture.gateway().findStatus(ChargeID.from(DATA_ID)))
                .thenThrow(new ResourceNotFoundException("Charge", DATA_ID));

        final var result = fixture.handler().handle("ts=" + TS + ",v1=" + V1, REQUEST_ID, DATA_ID, "payment");

        assertThat(result).isPresent();
        assertThat(result.get().isLeft()).isTrue();
        assertThat(result.get().getLeft().getErrors().get(0).message()).isEqualTo("Charge not found: " + DATA_ID);
    }

    @Test
    @DisplayName("Given non-payment topic, when handle, then ignores")
    void givenNonPaymentTopic_whenHandle_thenIgnores() {
        final var fixture = fixture();

        final var result = fixture.handler().handle(null, null, DATA_ID, "merchant_order");

        assertThat(result).isEmpty();
    }
}
