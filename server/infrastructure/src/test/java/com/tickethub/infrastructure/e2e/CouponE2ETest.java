package com.tickethub.infrastructure.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.E2ETest;
import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.authentication.persistence.RefreshSessionDocument;
import com.tickethub.infrastructure.coupon.persistence.CouponDocument;
import com.tickethub.infrastructure.customer.persistence.CustomerDocument;
import com.tickethub.infrastructure.operator.persistence.OperatorDocument;
import com.tickethub.infrastructure.order.persistence.OrderDocument;
import com.tickethub.infrastructure.partner.persistence.PartnerDocument;
import com.tickethub.infrastructure.section.persistence.SectionDocument;
import com.tickethub.infrastructure.show.persistence.ShowDocument;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@E2ETest
@DisplayName("Coupon E2 e")
class CouponE2ETest extends ContainerSupport {

    private static final String MASTER_HASH = "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        MongoCleanUpExtension.cleanCollections(
                mongoTemplate,
                OperatorDocument.COLLECTION,
                CustomerDocument.COLLECTION,
                PartnerDocument.COLLECTION,
                CouponDocument.COLLECTION,
                ShowDocument.COLLECTION,
                SectionDocument.COLLECTION,
                SpotDocument.COLLECTION,
                OrderDocument.COLLECTION,
                RefreshSessionDocument.COLLECTION);
        final var now = Instant.now();
        mongoTemplate.save(
                new OperatorDocument(
                        UUID.randomUUID().toString(),
                        "Master",
                        "master@tickethub.local",
                        MASTER_HASH,
                        now,
                        now,
                        null,
                        "test",
                        "test"),
                OperatorDocument.COLLECTION);
    }

    private String login(final String identifier, final String password) throws Exception {
        final var response = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"%s\",\"password\":\"%s\"}".formatted(identifier, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String ownerId(final String token) throws Exception {
        return objectMapper
                .readTree(new String(
                        java.util.Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8))
                .get("ownerId")
                .asText();
    }

    @Test
    @DisplayName("Given section coupon, when checkout with code, then totals discounted")
    void givenSectionCoupon_whenCheckoutWithCode_thenTotalsDiscounted() throws Exception {
        final var masterToken = login("master@tickethub.local", "admin-local");

        final var partnerCreated = mvc.perform(post("/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cinema Nova","cnpj":"11222333000181",\
                                "address":{"street":"Rua X","number":"42","neighborhood":"Bairro X",\
                                "city":"Cidade X","state":"XX","country":"Brasil","zipCode":"01305-000"},\
                                "email":"coupon-partner@domain.com","password":"secret-123"}\
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final var partnerId = objectMapper.readTree(partnerCreated).get("id").asText();
        mvc.perform(post("/partners/" + partnerId + "/approve").header("Authorization", "Bearer " + masterToken))
                .andExpect(status().isOk());
        final var partnerToken = login("coupon-partner@domain.com", "secret-123");

        final var showCreated = mvc.perform(post("/shows")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partnerId":"%s","name":"Festival","description":"Main event",\
                                "date":"2027-01-15T20:00:00-03:00",\
                                "address":{"street":"Rua X","number":"42","neighborhood":"Bairro X",\
                                "city":"Cidade X","state":"XX","country":"Brasil","zipCode":"01305-000"},\
                                "totalSpots":0}\
                                """.formatted(partnerId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final var showId = objectMapper.readTree(showCreated).get("id").asText();

        mvc.perform(post("/shows/" + showId + "/sections")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Pista","description":"Setor",\
                                "totalSpots":0,"price":{"value":50.00,"currency":"BRL"}}\
                                """))
                .andExpect(status().isOk());

        final var sections = mvc.perform(get("/sections?search=Pista&page=0&perPage=10"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final var sectionId =
                objectMapper.readTree(sections).get("items").get(0).get("id").asText();

        mvc.perform(post("/coupons")
                        .header("Authorization", "Bearer " + masterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"pista10","sectionId":"%s","kind":"PERCENT","percent":10}\
                                """.formatted(sectionId)))
                .andExpect(status().isCreated());

        final var firstSpot = mvc.perform(post("/spots")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sectionId\":\"%s\",\"location\":\"A1\"}".formatted(sectionId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final var spotId = objectMapper.readTree(firstSpot).get("id").asText();

        final var secondSpot = mvc.perform(post("/spots")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sectionId\":\"%s\",\"location\":\"A2\"}".formatted(sectionId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final var secondSpotId = objectMapper.readTree(secondSpot).get("id").asText();

        mvc.perform(post("/shows/" + showId + "/publish-all")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        mvc.perform(
                        post("/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"cpf\":\"52998224725\",\"name\":\"Buyer\",\"email\":\"coupon-buyer@domain.com\",\"password\":\"secret-123\"}"))
                .andExpect(status().isCreated());
        final var buyerToken = login("coupon-buyer@domain.com", "secret-123");
        final var buyerId = ownerId(buyerToken);

        final var order = mvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + buyerToken)
                        .header("Idempotency-Key", "coupon-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"%s\",\"spotIds\":[\"%s\"],\"couponCode\":\"pista10\"}"
                                .formatted(buyerId, spotId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalValue").value(45.00))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertEquals("BRL", objectMapper.readTree(order).get("currency").asText());

        mvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + buyerToken)
                        .header("Idempotency-Key", "coupon-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"%s\",\"spotIds\":[\"%s\"],\"couponCode\":\"nope\"}"
                                .formatted(buyerId, secondSpotId)))
                .andExpect(status().isUnprocessableEntity());
    }
}
