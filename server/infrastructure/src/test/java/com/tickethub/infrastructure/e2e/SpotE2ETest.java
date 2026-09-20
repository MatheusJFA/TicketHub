package com.tickethub.infrastructure.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import com.tickethub.infrastructure.section.persistence.SectionDocument;

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
        // Standalone spot creation requires an existing parent section.
        mongoTemplate.insert(new SectionDocument("section-1", null, null, false, 0, 0, null,
                null, null, null, null, null, null, null, null), SectionDocument.COLLECTION);

        mvc.perform(post("/spots")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sectionId\":\"section-1\",\"location\":\"A1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        // Login and creation are both audited and the writes land asynchronously.
        final var entry = awaitAuditEntries(2).stream()
                .filter(candidate -> "DefaultCreateSpotUseCase".equals(candidate.getString("action")))
                .findFirst()
                .orElseThrow();
        assertEquals("SUCCESS", entry.getString("outcome"));
        assertEquals("admin", entry.getString("actor"));
    }

    private List<Document> awaitAuditEntries(final int expected) throws InterruptedException {
        final var deadline = System.currentTimeMillis() + 10_000;
        while (true) {
            final var entries = mongoTemplate.findAll(Document.class, MongoAuditTrail.COLLECTION);
            if (entries.size() >= expected || System.currentTimeMillis() > deadline) {
                return entries;
            }
            Thread.sleep(100);
        }
    }

    @Test
    void givenPublicCatalog_whenListSpots_thenSucceeds() throws Exception {
        mvc.perform(get("/spots"))
                .andExpect(status().isOk());
    }
}
