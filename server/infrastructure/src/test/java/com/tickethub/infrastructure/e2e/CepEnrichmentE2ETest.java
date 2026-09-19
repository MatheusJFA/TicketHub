package com.tickethub.infrastructure.e2e;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.E2ETest;
import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.customer.persistence.CustomerDocument;
import com.tickethub.infrastructure.partner.persistence.PartnerDocument;

@E2ETest
@TestPropertySource(properties = "tickethub.cep.base-url=http://127.0.0.1:1")
class CepEnrichmentE2ETest extends ContainerSupport {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        MongoCleanUpExtension.cleanCollections(mongoTemplate, PartnerDocument.COLLECTION,
                CustomerDocument.COLLECTION);
    }

    @Test
    void givenUnreachableCepProvider_whenCreatePartner_thenPersistsUserAddress() throws Exception {
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
                .andReturn().getResponse().getContentAsString();
        final var id = objectMapper.readTree(created).get("id").asText();
        final var login = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"admin\",\"password\":\"admin-local\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        final var adminToken = objectMapper.readTree(login).get("accessToken").asText();

        mvc.perform(get("/partners/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address.street").value("Rua X"))
                .andExpect(jsonPath("$.address.number").value("42"));
    }
}
