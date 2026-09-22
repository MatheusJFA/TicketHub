package com.tickethub.infrastructure.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tickethub.application.Either;
import com.tickethub.application.payment.confirm.ConfirmPaymentOutput;
import com.tickethub.application.payment.confirm.ConfirmPaymentUseCase;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;
import com.tickethub.infrastructure.ControllerTest;
import com.tickethub.infrastructure.payment.mercadopago.InvalidWebhookSignatureException;
import com.tickethub.infrastructure.payment.mercadopago.MercadoPagoWebhookHandler;

import java.util.Optional;

@ControllerTest(controllers = {PaymentController.class, MercadoPagoWebhookController.class})
@Import({})
@DisplayName("Payment controller")
class PaymentControllerTest {
    @Autowired MockMvc mvc;

    @MockitoBean ConfirmPaymentUseCase confirmPayment;

    @MockitoBean MercadoPagoWebhookHandler mercadoPagoWebhook;

    @Test
    @DisplayName("Given paid charge, when webhook arrives without token, then settles order")
    void givenPaidCharge_whenWebhookArrives_thenSettlesOrder() throws Exception {
        when(confirmPayment.execute(any())).thenReturn(Either.right(
                new ConfirmPaymentOutput("order-1", "PAID")));

        mvc.perform(post("/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chargeId\":\"ch_123\",\"status\":\"PAID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order-1"))
                .andExpect(jsonPath("$.orderStatus").value("PAID"));
    }

    @Test
    @DisplayName("Given unknown charge, when webhook arrives, then returns not found")
    void givenUnknownCharge_whenWebhookArrives_thenReturnsNotFound() throws Exception {
        when(confirmPayment.execute(any())).thenReturn(Either.left(
                Notification.create(new Error("Charge not found: ch_missing"))));

        mvc.perform(post("/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chargeId\":\"ch_missing\",\"status\":\"PAID\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Given missing status, when webhook arrives, then returns bad request")
    void givenMissingStatus_whenWebhookArrives_thenReturnsBadRequest() throws Exception {
        mvc.perform(post("/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chargeId\":\"ch_123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Given verified MP notification, when webhook arrives, then settles order")
    void givenVerifiedMpNotification_whenWebhookArrives_thenSettlesOrder() throws Exception {
        when(mercadoPagoWebhook.handle(any(), any(), any(), any())).thenReturn(Optional.of(Either.right(
                new ConfirmPaymentOutput("order-1", "PAID"))));

        mvc.perform(post("/payments/mercadopago")
                        .queryParam("data.id", "123")
                        .queryParam("type", "payment")
                        .header("x-signature", "ts=1,v1=abc")
                        .header("x-request-id", "req-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"payment\",\"data\":{\"id\":\"123\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order-1"))
                .andExpect(jsonPath("$.orderStatus").value("PAID"));
    }

    @Test
    @DisplayName("Given forged MP notification, when webhook arrives, then returns unauthorized")
    void givenForgedMpNotification_whenWebhookArrives_thenReturnsUnauthorized() throws Exception {
        when(mercadoPagoWebhook.handle(any(), any(), any(), any()))
                .thenThrow(new InvalidWebhookSignatureException("Invalid webhook signature"));

        mvc.perform(post("/payments/mercadopago")
                        .queryParam("data.id", "123")
                        .header("x-signature", "ts=1,v1=forged")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"payment\",\"data\":{\"id\":\"123\"}}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Given non-payment MP topic, when webhook arrives, then acknowledges without effect")
    void givenNonPaymentTopic_whenWebhookArrives_thenAcknowledges() throws Exception {
        when(mercadoPagoWebhook.handle(any(), any(), any(), any())).thenReturn(Optional.empty());

        mvc.perform(post("/payments/mercadopago")
                        .queryParam("data.id", "123")
                        .queryParam("type", "merchant_order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"merchant_order\",\"data\":{\"id\":\"123\"}}"))
                .andExpect(status().isOk());
    }
}
