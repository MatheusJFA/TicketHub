package com.tickethub.infrastructure.events;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.tickethub.infrastructure.webhooks.PartnerWebhookDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("Partner webhook listener")
class PartnerWebhookListenerTest {

    @Mock
    private PartnerWebhookDispatcher dispatcher;

    private PartnerWebhookListener listener;

    @BeforeEach
    void setUp() {
        listener = new PartnerWebhookListener(dispatcher, new ObjectMapper());
    }

    @Test
    @DisplayName("Given paid message, when receive, then dispatches paid")
    void givenPaidMessage_whenReceive_thenDispatchesPaid() {
        listener.onMessage("{\"type\":\"OrderPaid\",\"orderId\":\"order-1\",\"occurredOn\":\"2026-09-24T10:00:00Z\"}");

        verify(dispatcher).dispatchPaid("order-1", "2026-09-24T10:00:00Z");
    }

    @Test
    @DisplayName("Given refunded message, when receive, then dispatches refunded")
    void givenRefundedMessage_whenReceive_thenDispatchesRefunded() {
        listener.onMessage(
                "{\"type\":\"OrderRefunded\",\"orderId\":\"order-1\",\"occurredOn\":\"2026-09-24T10:00:00Z\"}");

        verify(dispatcher).dispatchRefunded("order-1", "2026-09-24T10:00:00Z");
    }

    @Test
    @DisplayName("Given other type, when receive, then ignores")
    void givenOtherType_whenReceive_thenIgnores() {
        listener.onMessage(
                "{\"type\":\"SpotsGenerationRequested\",\"showId\":\"show-1\",\"sectionId\":\"section-1\",\"sectionCode\":\"B\",\"totalSpots\":10}");

        verify(dispatcher, never())
                .dispatchPaid(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(dispatcher, never())
                .dispatchRefunded(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Given garbage, when receive, then ignores")
    void givenGarbage_whenReceive_thenIgnores() {
        listener.onMessage("not-json");

        verify(dispatcher, never())
                .dispatchPaid(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
