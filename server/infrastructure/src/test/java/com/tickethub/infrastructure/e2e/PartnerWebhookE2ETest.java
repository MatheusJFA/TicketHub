package com.tickethub.infrastructure.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sun.net.httpserver.HttpServer;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.E2ETest;
import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.authentication.persistence.RefreshSessionDocument;
import com.tickethub.infrastructure.customer.persistence.CustomerDocument;
import com.tickethub.infrastructure.operator.persistence.OperatorDocument;
import com.tickethub.infrastructure.order.persistence.OrderDocument;
import com.tickethub.infrastructure.partner.persistence.PartnerDocument;
import com.tickethub.infrastructure.section.persistence.SectionDocument;
import com.tickethub.infrastructure.show.persistence.ShowDocument;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@E2ETest
@DisplayName("Partner webhook E2 e")
class PartnerWebhookE2ETest extends ContainerSupport {

    private static final String MASTER_HASH = "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private HttpServer hookServer;
    private ConcurrentLinkedQueue<Captured> captured;

    record Captured(Map<String, String> headers, String body) {}

    @BeforeEach
    void cleanUp() throws Exception {
        MongoCleanUpExtension.cleanCollections(
                mongoTemplate,
                OperatorDocument.COLLECTION,
                CustomerDocument.COLLECTION,
                PartnerDocument.COLLECTION,
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
        captured = new ConcurrentLinkedQueue<>();
        hookServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        hookServer.createContext("/hook", exchange -> {
            final var headers = new java.util.HashMap<String, String>();
            exchange.getRequestHeaders().forEach((name, values) -> headers.put(name.toLowerCase(), values.get(0)));
            final var body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            captured.add(new Captured(headers, body));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        hookServer.start();
    }

    @AfterEach
    void stopServer() {
        if (hookServer != null) {
            hookServer.stop(0);
        }
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

    private String hookUrl() {
        return "http://127.0.0.1:" + hookServer.getAddress().getPort() + "/hook";
    }

    @Test
    @DisplayName("Given paid order, when confirm, then partner webhook is delivered signed")
    void givenPaidOrder_whenConfirm_thenPartnerWebhookIsDeliveredSigned() throws Exception {
        final var masterToken = login("master@tickethub.local", "admin-local");

        final var partnerCreated = mvc.perform(post("/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cinema Nova","cnpj":"11222333000181",\
                                "address":{"street":"Rua X","number":"42","neighborhood":"Bairro X",\
                                "city":"Cidade X","state":"XX","country":"Brasil","zipCode":"01305-000"},\
                                "email":"hook-partner@domain.com","password":"secret-123"}\
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final var partnerId = objectMapper.readTree(partnerCreated).get("id").asText();
        mvc.perform(post("/partners/" + partnerId + "/approve").header("Authorization", "Bearer " + masterToken))
                .andExpect(status().isOk());
        final var partnerToken = login("hook-partner@domain.com", "secret-123");

        mvc.perform(put("/partners/" + partnerId + "/webhook")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"webhookUrl\":\"%s\",\"webhookSecret\":\"hook-secret\"}".formatted(hookUrl())))
                .andExpect(status().isOk());

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

        final var spotCreated = mvc.perform(post("/spots")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sectionId\":\"%s\",\"location\":\"A1\"}".formatted(sectionId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final var spotId = objectMapper.readTree(spotCreated).get("id").asText();

        mvc.perform(post("/shows/" + showId + "/publish-all")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        mvc.perform(
                        post("/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"cpf\":\"52998224725\",\"name\":\"Buyer\",\"email\":\"hook-buyer@domain.com\",\"password\":\"secret-123\"}"))
                .andExpect(status().isCreated());
        final var buyerToken = login("hook-buyer@domain.com", "secret-123");
        final var buyerId = objectMapper
                .readTree(new String(
                        java.util.Base64.getUrlDecoder().decode(buyerToken.split("\\.")[1]), StandardCharsets.UTF_8))
                .get("ownerId")
                .asText();

        final var orderCreated = mvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + buyerToken)
                        .header("Idempotency-Key", "hook-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"%s\",\"spotIds\":[\"%s\"]}".formatted(buyerId, spotId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final var orderId = objectMapper.readTree(orderCreated).get("orderId").asText();

        final var paid = mvc.perform(post("/orders/" + orderId + "/pay")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final var chargeId = objectMapper.readTree(paid).get("chargeId").asText();

        mvc.perform(post("/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chargeId\":\"" + chargeId + "\",\"status\":\"PAID\"}"))
                .andExpect(status().isOk());

        final var delivery = awaitDelivery();
        assertNotNull(delivery);
        assertEquals("order.paid", delivery.headers().get("x-tickethub-event"));
        assertEquals(orderId + ":order.paid", delivery.headers().get("x-tickethub-delivery"));
        final var payload = objectMapper.readTree(delivery.body());
        assertEquals(orderId, payload.get("orderId").asText());
        assertEquals(showId, payload.get("showId").asText());
        assertEquals("50.00", payload.get("total").asText());
        final var expectedSignature = hmacHex("hook-secret", delivery.body());
        assertEquals(expectedSignature, delivery.headers().get("x-tickethub-signature"));
    }

    private Captured awaitDelivery() throws Exception {
        final var deadline = System.currentTimeMillis() + 60_000;
        while (System.currentTimeMillis() < deadline) {
            final var delivery = captured.poll();
            if (delivery != null) {
                return delivery;
            }
            Thread.sleep(500);
        }
        return null;
    }

    private static String hmacHex(final String secret, final String message) throws Exception {
        final var mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return java.util.HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
    }
}
