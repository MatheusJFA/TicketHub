package com.tickethub.infrastructure.e2e;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.E2ETest;
import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.authentication.persistence.RefreshSessionDocument;
import com.tickethub.infrastructure.customer.persistence.CustomerDocument;
import com.tickethub.infrastructure.operator.persistence.OperatorDocument;
import com.tickethub.infrastructure.partner.persistence.PartnerDocument;
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
@DisplayName("Operator E2 e")
class OperatorE2ETest extends ContainerSupport {

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
                RefreshSessionDocument.COLLECTION);
        seedOperator("master@tickethub.local", MASTER_HASH);
    }

    private void seedOperator(final String email, final String passwordHash) {
        final var now = Instant.now();
        mongoTemplate.save(
                new OperatorDocument(
                        UUID.randomUUID().toString(), "Master", email, passwordHash, now, now, null, "test", "test"),
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

    @Test
    @DisplayName("Given master, when creates operator, then operator can login")
    void givenMaster_whenCreatesOperator_thenOperatorCanLogin() throws Exception {
        final var masterToken = login("master@tickethub.local", "admin-local");

        mvc.perform(
                        post("/operators")
                                .header("Authorization", "Bearer " + masterToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"name\":\"Backoffice\",\"email\":\"backoffice@tickethub.local\",\"password\":\"secret-123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        // New operator authenticates with ADMIN authorities: can approve partners.
        final var operatorToken = login("backoffice@tickethub.local", "secret-123");
        final var createdPartner = mvc.perform(post("/partners")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cinema Nova","cnpj":"11222333000181",\
                                "address":{"street":"Rua X","number":"42","neighborhood":"Bairro X",\
                                "city":"Cidade X","state":"XX","country":"Brasil","zipCode":"01305-000"},\
                                "email":"op-partner@domain.com","password":"secret-123"}\
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final var partnerId = objectMapper.readTree(createdPartner).get("id").asText();

        mvc.perform(post("/partners/" + partnerId + "/approve").header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Given customer, when creates operator, then returns forbidden")
    void givenCustomer_whenCreatesOperator_thenReturnsForbidden() throws Exception {
        mvc.perform(
                        post("/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"cpf\":\"52998224725\",\"name\":\"Maria Silva\",\"email\":\"op-customer@domain.com\",\"password\":\"secret-123\"}"))
                .andExpect(status().isCreated());
        final var customerToken = login("op-customer@domain.com", "secret-123");

        mvc.perform(
                        post("/operators")
                                .header("Authorization", "Bearer " + customerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"name\":\"Intruso\",\"email\":\"intruso@tickethub.local\",\"password\":\"secret-123\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Given anonymous, when creates operator, then returns unauthorized")
    void givenAnonymous_whenCreatesOperator_thenReturnsUnauthorized() throws Exception {
        mvc.perform(
                        post("/operators")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"name\":\"Intruso\",\"email\":\"anon@tickethub.local\",\"password\":\"secret-123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Given anonymous, when requests partner, then stays pending without login")
    void givenAnonymous_whenRequestsPartner_thenStaysPendingWithoutLogin() throws Exception {
        mvc.perform(post("/partners").contentType(MediaType.APPLICATION_JSON).content("""
                                {"name":"Cinema Nova","cnpj":"04252011000110",\
                                "address":{"street":"Rua X","number":"42","neighborhood":"Bairro X",\
                                "city":"Cidade X","state":"XX","country":"Brasil","zipCode":"01305-000"},\
                                "email":"anon-partner@domain.com","password":"secret-123"}\
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"anon-partner@domain.com\",\"password\":\"secret-123\"}"))
                .andExpect(status().isUnauthorized());
    }
}
