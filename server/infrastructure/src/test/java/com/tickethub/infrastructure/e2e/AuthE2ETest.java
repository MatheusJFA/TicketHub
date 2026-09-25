package com.tickethub.infrastructure.e2e;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.E2ETest;
import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.authentication.persistence.RefreshSessionDocument;
import com.tickethub.infrastructure.customer.persistence.CustomerDocument;
import com.tickethub.infrastructure.operator.persistence.OperatorDocument;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@E2ETest
@DisplayName("Auth E2 e")
class AuthE2ETest extends ContainerSupport {

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
                CustomerDocument.COLLECTION,
                RefreshSessionDocument.COLLECTION,
                OperatorDocument.COLLECTION);
    }

    private void seedOperator(final String email, final String passwordHash) {
        final var now = Instant.now();
        mongoTemplate.save(
                new OperatorDocument(
                        java.util.UUID.randomUUID().toString(),
                        "Admin Local",
                        email,
                        passwordHash,
                        now,
                        now,
                        null,
                        "test",
                        "test"),
                OperatorDocument.COLLECTION);
    }

    private JsonNode registerCustomer(final String cpf, final String email) throws Exception {
        final var response = mvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpf":"%s","name":"Maria Silva",\
                                "email":"%s","password":"secret-123"}\
                                """.formatted(cpf, email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode login(final String identifier, final String password) throws Exception {
        final var response = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"%s"}\
                                """.formatted(identifier, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode refresh(final String refreshToken) throws Exception {
        final var response = mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(refreshToken)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    @Test
    @DisplayName("Given registered customer, when login, then access token authorizes self service")
    void givenRegisteredCustomer_whenLogin_thenAccessTokenAuthorizesSelfService() throws Exception {
        final var customerId = registerCustomer("52998224725", "auth-login@domain.com")
                .get("id")
                .asText();
        final var session = login("auth-login@domain.com", "secret-123");

        mvc.perform(patch("/customers/" + customerId + "/name")
                        .header(
                                "Authorization",
                                "Bearer " + session.get("accessToken").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Maria Souza\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId));
    }

    @Test
    @DisplayName("Given wrong password, when login, then returns401")
    void givenWrongPassword_whenLogin_thenReturns401() throws Exception {
        registerCustomer("12345678909", "auth-wrong@domain.com");

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"auth-wrong@domain.com\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors[0].message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("Given valid refresh token, when refresh, then rotates session")
    void givenValidRefreshToken_whenRefresh_thenRotatesSession() throws Exception {
        registerCustomer("11144477735", "auth-rotate@domain.com");
        final var session = login("auth-rotate@domain.com", "secret-123");
        final var firstRefresh = session.get("refreshToken").asText();

        final var rotated = refresh(firstRefresh);

        assertNotEquals(firstRefresh, rotated.get("refreshToken").asText());

        // Old token was rotated: reuse is rejected and revokes the family.
        mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(firstRefresh)))
                .andExpect(status().isUnauthorized());

        // The rotated token belonged to the same family: also revoked.
        mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}"
                                .formatted(rotated.get("refreshToken").asText())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Given refresh token, when logout, then session is revoked")
    void givenRefreshToken_whenLogout_thenSessionIsRevoked() throws Exception {
        registerCustomer("98765432100", "auth-logout@domain.com");
        final var session = login("auth-logout@domain.com", "secret-123");
        final var refreshToken = session.get("refreshToken").asText();

        mvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(refreshToken)))
                .andExpect(status().isNoContent());

        mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Given access token, when logout, then access token is revoked")
    void givenAccessToken_whenLogout_thenAccessTokenIsRevoked() throws Exception {
        final var customerId = registerCustomer("52998224725", "auth-logout-access@domain.com")
                .get("id")
                .asText();
        final var session = login("auth-logout-access@domain.com", "secret-123");
        final var accessToken = session.get("accessToken").asText();
        final var refreshToken = session.get("refreshToken").asText();

        mvc.perform(patch("/customers/" + customerId + "/name")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Maria Souza\"}"))
                .andExpect(status().isOk());

        mvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\",\"accessToken\":\"%s\"}"
                                .formatted(refreshToken, accessToken)))
                .andExpect(status().isNoContent());

        mvc.perform(patch("/customers/" + customerId + "/name")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Maria Souza\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Given operator account, when login, then returns admin session")
    void givenOperatorAccount_whenLogin_thenReturnsAdminSession() throws Exception {
        seedOperator("admin@tickethub.local", "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS");
        final MvcResult login = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"admin@tickethub.local\",\"password\":\"admin-local\"}"))
                .andExpect(status().isOk())
                .andReturn();

        final var session = objectMapper.readTree(login.getResponse().getContentAsString());
        mvc.perform(patch("/customers/any-id/name")
                        .header(
                                "Authorization",
                                "Bearer " + session.get("accessToken").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Maria Souza\"}"))
                .andExpect(status().isNotFound());
    }
}
