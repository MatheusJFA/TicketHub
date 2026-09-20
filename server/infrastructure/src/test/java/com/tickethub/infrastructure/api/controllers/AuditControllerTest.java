package com.tickethub.infrastructure.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tickethub.domain.pagination.Pagination;
import com.tickethub.infrastructure.ControllerTest;
import com.tickethub.infrastructure.audit.AuditLogReader;
import com.tickethub.infrastructure.audit.AuditLogResponse;
import com.tickethub.infrastructure.audit.AuditOutcome;
import com.tickethub.infrastructure.security.TestTokens;

@ControllerTest(controllers = AuditController.class)
class AuditControllerTest {

    @Autowired
    MockMvc mvc;

    @Value("${tickethub.security.jwt.secret}")
    String jwtSecret;

    private String bearer(final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, authorities);
    }

    @MockitoBean
    AuditLogReader reader;

    private static AuditLogResponse response() {
        return new AuditLogResponse("entry-1", Instant.parse("2027-01-15T20:00:00Z"), "corr-1",
                "alice", "DefaultCreateSpotUseCase", "[spot-1]", AuditOutcome.SUCCESS, null, 12);
    }

    @Test
    void givenAdmin_whenList_thenReturnsPage() throws Exception {
        when(reader.search(any()))
                .thenReturn(new Pagination<>(0, 10, 1, List.of(response())));

        mvc.perform(get("/audit-logs").header("Authorization", bearer("ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].action").value("DefaultCreateSpotUseCase"))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    void givenFilters_whenList_thenDelegatesToReader() throws Exception {
        when(reader.search(any()))
                .thenReturn(new Pagination<>(0, 10, 0, List.of()));

        mvc.perform(get("/audit-logs")
                        .header("Authorization", bearer("ROLE_ADMIN"))
                        .param("action", "DefaultCreateSpotUseCase")
                        .param("actor", "alice")
                        .param("outcome", "success")
                        .param("correlationId", "corr-1")
                        .param("from", "2027-01-01T00:00:00Z")
                        .param("to", "2027-02-01T00:00:00Z"))
                .andExpect(status().isOk());
    }

    @Test
    void givenNonAdmin_whenList_thenReturnsForbidden() throws Exception {
        mvc.perform(get("/audit-logs").header("Authorization", bearer("spot:write")))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenNoToken_whenList_thenReturnsUnauthorized() throws Exception {
        mvc.perform(get("/audit-logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenUnknownOutcome_whenList_thenReturnsUnprocessable() throws Exception {
        mvc.perform(get("/audit-logs")
                        .header("Authorization", bearer("ROLE_ADMIN"))
                        .param("outcome", "bogus"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void givenInvalidPagination_whenList_thenReturnsUnprocessable() throws Exception {
        mvc.perform(get("/audit-logs")
                        .header("Authorization", bearer("ROLE_ADMIN"))
                        .param("perPage", "999"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void givenInvalidSort_whenList_thenReturnsUnprocessable() throws Exception {
        mvc.perform(get("/audit-logs")
                        .header("Authorization", bearer("ROLE_ADMIN"))
                        .param("sort", "hackerField"))
                .andExpect(status().isUnprocessableEntity());
    }
}
