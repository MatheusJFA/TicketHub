package com.tickethub.infrastructure.payment.mercadopago;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.payment.ChargeID;
import com.tickethub.domain.core.payment.ChargeStatus;
import com.tickethub.domain.shared.Money;
import com.tickethub.infrastructure.shared.http.BaseHttpClient;
import com.tickethub.infrastructure.shared.http.HttpUpstreamException;

@DisplayName("Mercado Pago payment gateway")
class MercadoPagoPaymentGatewayTest {

    private static final String BASE_URL = "https://api.mercadopago.com";

    private record Fixture(MercadoPagoPaymentGateway gateway, MockRestServiceServer server) {
    }

    private Fixture fixture() {
        return fixtureWithCustomer("maria@domain.com");
    }

    private Fixture fixtureWithCustomer(final String email) {
        final var builder = BaseHttpClient.preparedBuilder(RestClient.builder(), BASE_URL);
        final var server = MockRestServiceServer.bindTo(builder).build();
        final var properties = new MercadoPagoProperties();
        properties.setPayerEmail("buyer@tickethub.local");
        final var orderGateway = mock(OrderGateway.class);
        final var customerGateway = mock(CustomerGateway.class);
        final var order = mock(Order.class);
        when(order.getCustomerId()).thenReturn(CustomerID.from("cust-1"));
        when(orderGateway.findById(OrderID.from("order-1"))).thenReturn(Optional.of(order));
        if (email != null) {
            final var customer = mock(Customer.class);
            when(customer.getEmail()).thenReturn(com.tickethub.domain.shared.Email.create(email));
            when(customerGateway.findById(CustomerID.from("cust-1"))).thenReturn(Optional.of(customer));
        } else {
            when(customerGateway.findById(CustomerID.from("cust-1"))).thenReturn(Optional.empty());
        }
        return new Fixture(
                new MercadoPagoPaymentGateway(new MercadoPagoClient(builder.build()), properties,
                        orderGateway, customerGateway),
                server);
    }

    private Money fifty() {
        return Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));
    }

    @Test
    @DisplayName("Given pending PIX payment, when create charge, then returns PENDING with QR code")
    void givenPendingPix_whenCreateCharge_thenReturnsPendingWithQrCode() {
        final var fixture = fixture();
        fixture.server().expect(requestTo(BASE_URL + "/v1/payments"))
                .andExpect(content().string(Matchers.containsString("maria@domain.com")))
                .andRespond(withSuccess("""
                        {"id":123,"status":"pending","transaction_amount":50.00,\
                        "currency_id":"BRL","external_reference":"order-1",\
                        "point_of_interaction":{"transaction_data":{\
                        "qr_code":"000201010212","qr_code_base64":"e30="}}}\
                        """, MediaType.APPLICATION_JSON));

        final var charge = fixture.gateway()
                .createCharge(OrderID.from("order-1"), fifty());

        assertEquals("123", charge.getChargeId().getValue());
        assertEquals(ChargeStatus.PENDING, charge.getStatus());
        assertEquals("000201010212", charge.getPaymentCode());
        fixture.server().verify();
    }

    @Test
    @DisplayName("Given missing customer, when create charge, then falls back to configured payer email")
    void givenMissingCustomer_whenCreateCharge_thenFallsBackToConfiguredEmail() {
        final var fixture = fixtureWithCustomer(null);
        fixture.server().expect(requestTo(BASE_URL + "/v1/payments"))
                .andExpect(content().string(Matchers.containsString("buyer@tickethub.local")))
                .andRespond(withSuccess("""
                        {"id":124,"status":"pending","transaction_amount":50.00,\
                        "currency_id":"BRL","external_reference":"order-1"}\
                        """, MediaType.APPLICATION_JSON));

        final var charge = fixture.gateway()
                .createCharge(OrderID.from("order-1"), fifty());

        assertEquals("124", charge.getChargeId().getValue());
        assertEquals(ChargeStatus.PENDING, charge.getStatus());
        fixture.server().verify();
    }

    @Test
    @DisplayName("Given approved payment, when find status, then returns PAID")
    void givenApproved_whenFindStatus_thenReturnsPaid() {
        final var fixture = fixture();
        fixture.server().expect(requestTo(BASE_URL + "/v1/payments/123"))
                .andRespond(withSuccess("""
                        {"id":123,"status":"approved","transaction_amount":50.00,\
                        "currency_id":"BRL","external_reference":"order-1",\
                        "date_approved":"2026-09-22T10:05:00.000-03:00"}\
                        """, MediaType.APPLICATION_JSON));

        final var charge = fixture.gateway().findStatus(ChargeID.from("123"));

        assertEquals(ChargeStatus.PAID, charge.getStatus());
        assertEquals("order-1", charge.getOrderId().getValue());
        assertEquals(java.time.Instant.parse("2026-09-22T13:05:00Z"), charge.getApprovedAt());
        fixture.server().verify();
    }

    @Test
    @DisplayName("Given rejected payment, when find status, then returns FAILED")
    void givenRejected_whenFindStatus_thenReturnsFailed() {
        final var fixture = fixture();
        fixture.server().expect(requestTo(BASE_URL + "/v1/payments/123"))
                .andRespond(withSuccess("""
                        {"id":123,"status":"rejected","transaction_amount":50.00,\
                        "currency_id":"BRL","external_reference":"order-1"}\
                        """, MediaType.APPLICATION_JSON));

        final var charge = fixture.gateway().findStatus(ChargeID.from("123"));

        assertEquals(ChargeStatus.FAILED, charge.getStatus());
        fixture.server().verify();
    }

    @Test
    @DisplayName("Given unknown status, when find status, then fails closed")
    void givenUnknownStatus_whenFindStatus_thenFailsClosed() {
        final var fixture = fixture();
        fixture.server().expect(requestTo(BASE_URL + "/v1/payments/123"))
                .andRespond(withSuccess("""
                        {"id":123,"status":"mystery","transaction_amount":50.00,\
                        "currency_id":"BRL","external_reference":"order-1"}\
                        """, MediaType.APPLICATION_JSON));

        assertThrows(HttpUpstreamException.class,
                () -> fixture.gateway().findStatus(ChargeID.from("123")));
        fixture.server().verify();
    }

    @Test
    @DisplayName("Given captured payment, when refund, then posts refund without error")
    void givenCapturedPayment_whenRefund_thenPostsRefund() {
        final var fixture = fixture();
        fixture.server().expect(requestTo(BASE_URL + "/v1/payments/123/refunds"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> fixture.gateway().refund(ChargeID.from("123")));
        fixture.server().verify();
    }
}
