package com.tickethub.infrastructure.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tickethub.application.Either;
import com.tickethub.application.order.cancel.CancelOrderOutput;
import com.tickethub.application.order.cancel.CancelOrderUseCase;
import com.tickethub.application.order.create.CreateOrderOutput;
import com.tickethub.application.order.create.CreateOrderUseCase;
import com.tickethub.application.payment.pay.PayOrderOutput;
import com.tickethub.application.payment.pay.PayOrderUseCase;
import com.tickethub.application.order.retrieve.get.GetOrderOutput;
import com.tickethub.application.order.retrieve.get.GetOrderUseCase;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;
import com.tickethub.infrastructure.ControllerTest;
import com.tickethub.infrastructure.security.TestTokens;

@ControllerTest(controllers = OrderController.class)
@Import({})
@DisplayName("Order controller")
class OrderControllerTest {
    @Autowired MockMvc mvc;
    @Value("${tickethub.security.jwt.secret}")
    String jwtSecret;

    private String bearer(final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, authorities);
    }

    @MockitoBean CreateOrderUseCase createOrder;
    @MockitoBean PayOrderUseCase payOrder;
    @MockitoBean CancelOrderUseCase cancelOrder;
    @MockitoBean GetOrderUseCase getOrder;

    private static CreateOrderOutput created() {
        return new CreateOrderOutput("order-1", "customer-1", "PENDING",
                new BigDecimal("100.00"), "BRL", Instant.parse("2026-09-20T12:15:00Z"),
                List.of("spot-1", "spot-2"));
    }

    @Test
    @DisplayName("Given valid items, when creates order, then returns created with location")
    void givenValidItems_whenCreatesOrder_thenReturnsCreated() throws Exception {
        when(createOrder.execute(any())).thenReturn(Either.right(created()));

        mvc.perform(post("/orders")
                        .header("Authorization", bearer("order:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"customer-1\",\"spotIds\":[\"spot-1\",\"spot-2\"]}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/orders/order-1"))
                .andExpect(jsonPath("$.orderId").value("order-1"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Given idempotency key, when creates order, then forwards key to use case")
    void givenIdempotencyKey_whenCreatesOrder_thenForwardsKey() throws Exception {
        when(createOrder.execute(any())).thenReturn(Either.right(created()));
        final var captor = org.mockito.ArgumentCaptor.forClass(
                com.tickethub.application.order.create.CreateOrderCommand.class);

        mvc.perform(post("/orders")
                        .header("Authorization", bearer("order:write"))
                        .header("Idempotency-Key", "key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"customer-1\",\"spotIds\":[\"spot-1\",\"spot-2\"]}"))
                .andExpect(status().isCreated());

        org.mockito.Mockito.verify(createOrder).execute(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().idempotencyKey())
                .isEqualTo("key-1");
    }

    @Test
    @DisplayName("Given unavailable spot, when creates order, then returns unprocessable")
    void givenUnavailableSpot_whenCreatesOrder_thenReturnsUnprocessable() throws Exception {
        when(createOrder.execute(any())).thenReturn(Either.left(
                Notification.create(new Error("Spot is unavailable"))));

        mvc.perform(post("/orders")
                        .header("Authorization", bearer("order:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"customer-1\",\"spotIds\":[\"spot-1\"]}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Given pending order, when pays order, then returns charge")
    void givenPendingOrder_whenPaysOrder_thenReturnsCharge() throws Exception {
        when(payOrder.execute(any())).thenReturn(Either.right(
                new PayOrderOutput("order-1", "ch_123", "PIX-MOCK-ch_123", "PENDING")));

        mvc.perform(post("/orders/order-1/pay")
                        .header("Authorization", bearer("order:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chargeId").value("ch_123"));
    }

    @Test
    @DisplayName("Given pending order, when cancels order, then returns cancelled order")
    void givenPendingOrder_whenCancelsOrder_thenReturnsCancelled() throws Exception {
        when(cancelOrder.execute(any())).thenReturn(Either.right(
                new CancelOrderOutput("order-1", "customer-1", "CANCELLED",
                        new BigDecimal("100.00"), "BRL", Instant.parse("2026-09-20T12:15:00Z"),
                        null, List.of("spot-1", "spot-2"))));

        mvc.perform(post("/orders/order-1/cancel")
                        .header("Authorization", bearer("order:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Given existing order, when gets order, then returns details")
    void givenExistingOrder_whenGetsOrder_thenReturnsDetails() throws Exception {
        when(getOrder.execute("order-1")).thenReturn(Either.right(
                new GetOrderOutput("order-1", "customer-1", "PAID",
                        new BigDecimal("100.00"), "BRL", Instant.parse("2026-09-20T12:15:00Z"),
                        "ch_123", List.of("spot-1", "spot-2"))));

        mvc.perform(get("/orders/order-1")
                        .header("Authorization", bearer("order:write")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.chargeId").value("ch_123"));
    }

    @Test
    @DisplayName("Given unknown order, when gets order, then returns not found")
    void givenUnknownOrder_whenGetsOrder_thenReturnsNotFound() throws Exception {
        when(getOrder.execute("order-9")).thenReturn(Either.left(
                Notification.create(new Error("Order not found: order-9"))));

        mvc.perform(get("/orders/order-9")
                        .header("Authorization", bearer("order:write")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Given no token, when creates order, then returns unauthorized")
    void givenNoToken_whenCreatesOrder_thenReturnsUnauthorized() throws Exception {
        mvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"customer-1\",\"spotIds\":[\"spot-1\"]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Given token without authority, when creates order, then returns forbidden")
    void givenTokenWithoutAuthority_whenCreatesOrder_thenReturnsForbidden() throws Exception {
        mvc.perform(post("/orders")
                        .header("Authorization", bearer("spot:read"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"customer-1\",\"spotIds\":[\"spot-1\"]}"))
                .andExpect(status().isForbidden());
    }
}
