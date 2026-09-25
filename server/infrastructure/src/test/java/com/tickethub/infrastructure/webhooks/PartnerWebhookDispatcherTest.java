package com.tickethub.infrastructure.webhooks;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.Order;
import com.tickethub.domain.core.order.OrderGateway;
import com.tickethub.domain.core.order.OrderItem;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.core.spot.SpotPlacement;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.shared.Location;
import com.tickethub.domain.shared.Money;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("Partner webhook dispatcher")
class PartnerWebhookDispatcherTest {

    private static final String HASH = "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS";

    @Mock
    private OrderGateway orders;

    @Mock
    private SpotGateway spots;

    @Mock
    private ShowGateway shows;

    @Mock
    private PartnerGateway partners;

    private PartnerWebhookDispatcher dispatcher(final RestClient restClient) {
        final var properties = new PartnerWebhookProperties();
        properties.setMaxAttempts(2);
        properties.setRetryWait(Duration.ofMillis(1));
        return new PartnerWebhookDispatcher(orders, spots, shows, partners, restClient, new ObjectMapper(), properties);
    }

    private Order givenChain(final String webhookUrl) {
        final var order = Order.create(
                CustomerID.from("customer-1"),
                List.of(OrderItem.of(
                        SpotID.from("spot-1"), Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL")))),
                Duration.ofMinutes(15));
        final var partner = Partner.create(
                        "Cinema Nova",
                        "11222333000181",
                        Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                        "cinema@domain.com",
                        HASH)
                .approve();
        if (webhookUrl != null) {
            partner.changeWebhook(webhookUrl, "hook-secret");
        }
        final var show = partner.createShow(
                "Festival",
                "Main event",
                OffsetDateTime.parse("2027-01-15T20:00:00-03:00"),
                Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                100);
        when(orders.findById(order.getId())).thenReturn(Optional.of(order));
        when(spots.findPlacement(SpotID.from("spot-1")))
                .thenReturn(Optional.of(new SpotPlacement(Spot.create(Location.create("A1")), "show-1", "section-1")));
        when(shows.findById(any())).thenReturn(Optional.of(show));
        when(partners.findById(show.getPartnerId())).thenReturn(Optional.of(partner));
        return order;
    }

    @Test
    @DisplayName("Given paid order, when dispatch, then posts signed payload")
    void givenPaidOrder_whenDispatch_thenPostsSignedPayload() {
        final var order = givenChain("https://partner.domain.com/hook");
        final var builder = RestClient.builder();
        final var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://partner.domain.com/hook"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Tickethub-Event", "order.paid"))
                .andExpect(header("X-Tickethub-Signature", org.hamcrest.Matchers.notNullValue()))
                .andExpect(header("X-Tickethub-Delivery", org.hamcrest.Matchers.notNullValue()))
                .andRespond(withSuccess());

        dispatcher(builder.build()).dispatchPaid(order.getId().getValue(), "2026-09-24T10:00:00Z");

        server.verify();
    }

    @Test
    @DisplayName("Given partner without webhook, when dispatch, then skips silently")
    void givenPartnerWithoutWebhook_whenDispatch_thenSkipsSilently() {
        final var order = givenChain(null);
        final var builder = RestClient.builder();
        final var server = MockRestServiceServer.bindTo(builder).build();

        dispatcher(builder.build()).dispatchPaid(order.getId().getValue(), "2026-09-24T10:00:00Z");

        server.verify();
    }

    @Test
    @DisplayName("Given failing endpoint, when dispatch, then throws after retries")
    void givenFailingEndpoint_whenDispatch_thenThrowsAfterRetries() {
        final var order = givenChain("https://partner.domain.com/hook");
        final var builder = RestClient.builder();
        final var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://partner.domain.com/hook")).andRespond(withServerError());
        server.expect(requestTo("https://partner.domain.com/hook")).andRespond(withServerError());
        final var orderId = order.getId().getValue();

        final var exception = assertThrows(
                PartnerWebhookException.class,
                () -> dispatcher(builder.build()).dispatchPaid(orderId, "2026-09-24T10:00:00Z"));

        assertTrue(exception.getMessage().contains(orderId));
        server.verify();
    }
}
