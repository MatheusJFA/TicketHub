package com.tickethub.infrastructure.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.ObjectMapper;

import com.tickethub.domain.shared.Money;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.E2ETest;
import com.tickethub.infrastructure.section.persistence.SectionDocument;
import com.tickethub.infrastructure.shared.persistence.MoneyDocument;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;
import com.tickethub.infrastructure.ticket.persistence.TicketDocument;

@E2ETest
@DisplayName("Checkout E2 e")
class CheckoutE2ETest extends ContainerSupport {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String givenPublishedSpots(final String... locations) {
        final var now = Instant.now();
        final var sectionId = "section-" + UUID.randomUUID().toString().substring(0, 8);
        mongoTemplate.insert(new SectionDocument(sectionId, "VIP", "Front stage", true, locations.length,
                0, MoneyDocument.from(Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"))),
                List.of(), "show-1", "partner-1", now, now, null, null, null),
                SectionDocument.COLLECTION);
        final var spotIds = new java.util.ArrayList<String>();
        for (final String location : locations) {
            final var spotId = "spot-" + UUID.randomUUID().toString().substring(0, 8);
            mongoTemplate.insert(new SpotDocument(spotId, location, true, true, false, "show-1",
                    sectionId, "partner-1", now, now, null, null, null), SpotDocument.COLLECTION);
            spotIds.add(spotId);
        }
        return String.join("\",\"", spotIds);
    }

    private String customerToken() throws Exception {
        final var email = "buyer-" + UUID.randomUUID().toString().substring(0, 8) + "@domain.com";
        mvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"52998224725\",\"name\":\"Buyer\",\"email\":\"" + email
                                + "\",\"password\":\"secret-123\"}"))
                .andExpect(status().isCreated());
        final var login = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + email + "\",\"password\":\"secret-123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(login).get("accessToken").asText();
    }

    private String customerId(final String token) throws Exception {
        final var payload = token.split("\\.")[1];
        final var claims = objectMapper.readTree(
                new String(java.util.Base64.getUrlDecoder().decode(payload)));
        return claims.get("ownerId").asText();
    }

    @Test
    @DisplayName("Given buyer and seats, when checkout, then pays and issues tickets")
    void givenBuyerAndSeats_whenCheckout_thenPaysAndIssuesTickets() throws Exception {
        final var token = customerToken();
        final var customerId = customerId(token);
        final var spotIds = givenPublishedSpots("A1", "A2");

        final MvcResult created = mvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "e2e-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"" + customerId + "\",\"spotIds\":[\"" + spotIds + "\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        final var orderId = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("orderId").asText();

        final var paid = mvc.perform(post("/orders/" + orderId + "/pay")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentCode").exists())
                .andReturn().getResponse().getContentAsString();
        final var chargeId = objectMapper.readTree(paid).get("chargeId").asText();

        mvc.perform(post("/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chargeId\":\"" + chargeId + "\",\"status\":\"PAID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("PAID"));

        mvc.perform(get("/orders/" + orderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        assertEquals(2, mongoTemplate.count(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("orderId").is(orderId)),
                TicketDocument.COLLECTION));
    }
}
