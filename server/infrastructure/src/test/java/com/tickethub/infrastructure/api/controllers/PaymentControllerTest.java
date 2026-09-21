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

@ControllerTest(controllers = PaymentController.class)
@Import({})
@DisplayName("Payment controller")
class PaymentControllerTest {
    @Autowired MockMvc mvc;

    @MockitoBean ConfirmPaymentUseCase confirmPayment;

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
}
