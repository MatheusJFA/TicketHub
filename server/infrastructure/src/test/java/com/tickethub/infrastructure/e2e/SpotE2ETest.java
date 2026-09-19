package com.tickethub.infrastructure.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.E2ETest;
import com.tickethub.infrastructure.audit.MongoAuditTrail;

@E2ETest
class SpotE2ETest extends ContainerSupport {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken() throws Exception {
        final var response = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"admin\",\"password\":\"admin-local\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    @Test
    void givenAdminCredentials_whenLogin_thenReturnsToken() throws Exception {
        adminToken();
    }

    @Test
    void givenNoToken_whenCreateSpot_thenReturns401() throws Exception {
        mvc.perform(post("/spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"A1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenAdminToken_whenCreateSpot_thenPersistsAndAudits() throws Exception {
        mvc.perform(post("/spots")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"A1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        final var entries = mongoTemplate.findAll(Document.class, MongoAuditTrail.COLLECTION);
        assertEquals(1, entries.size());
        final var entry = entries.get(0);
        assertEquals("DefaultCreateSpotUseCase", entry.getString("action"));
        assertEquals("SUCCESS", entry.getString("outcome"));
        assertEquals("admin", entry.getString("actor"));
    }

    @Test
    void givenPublicCatalog_whenListSpots_thenSucceeds() throws Exception {
        mvc.perform(get("/spots"))
                .andExpect(status().isOk());
    }
}
