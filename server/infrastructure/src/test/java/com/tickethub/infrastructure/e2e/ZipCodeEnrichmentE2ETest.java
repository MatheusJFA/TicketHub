package com.tickethub.infrastructure.e2e;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.E2ETest;
import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.customer.persistence.CustomerDocument;
import com.tickethub.infrastructure.operator.persistence.OperatorDocument;
import com.tickethub.infrastructure.partner.persistence.PartnerDocument;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@E2ETest
@TestPropertySource(properties = "tickethub.zipcode.base-url=http://127.0.0.1:1")
@DisplayName("ZIP code enrichment E2E")
class ZipCodeEnrichmentE2ETest extends ContainerSupport {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        MongoCleanUpExtension.cleanCollections(
                mongoTemplate, PartnerDocument.COLLECTION, CustomerDocument.COLLECTION, OperatorDocument.COLLECTION);
        final var now = Instant.now();
        mongoTemplate.save(
                new OperatorDocument(
                        java.util.UUID.randomUUID().toString(),
                        "Admin Local",
                        "admin@tickethub.local",
                        "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS",
                        now,
                        now,
                        null,
                        "test",
                        "test"),
                OperatorDocument.COLLECTION);
    }

    @Test
    @DisplayName("Given unreachable ZIP code provider, when create partner, then persists user address")
    void givenUnreachableZipCodeProvider_whenCreatePartner_thenPersistsUserAddress() throws Exception {
        final var created = mvc.perform(post("/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cinema Nova","cnpj":"11222333000181",\
                                "address":{"street":"Rua X","number":"42","neighborhood":"Bairro X",\
                                "city":"Cidade X","state":"XX","country":"Brasil","zipCode":"01305-000"},\
                                "email":"cep-failopen@domain.com","password":"secret-123"}\
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final var id = objectMapper.readTree(created).get("id").asText();

        final var login = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"admin@tickethub.local\",\"password\":\"admin-local\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final var adminToken = objectMapper.readTree(login).get("accessToken").asText();

        mvc.perform(get("/partners/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address.street").value("Rua X"))
                .andExpect(jsonPath("$.address.number").value("42"));
    }

    @Test
    @DisplayName("Given unreachable ZIP code provider, when lookup, then returns not found")
    void givenUnreachableZipCodeProvider_whenLookup_thenReturnsNotFound() throws Exception {
        mvc.perform(get("/zipcode/01305-000")).andExpect(status().isNotFound());
    }
}
