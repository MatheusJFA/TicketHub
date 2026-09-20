package com.tickethub.infrastructure.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("Audit controller")
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
    @DisplayName("Given admin, when list, then returns page")
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
    @DisplayName("Given filters, when list, then delegates to reader")
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
    @DisplayName("Given non admin, when list, then returns forbidden")
    void givenNonAdmin_whenList_thenReturnsForbidden() throws Exception {
        mvc.perform(get("/audit-logs").header("Authorization", bearer("spot:write")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Given no token, when list, then returns unauthorized")
    void givenNoToken_whenList_thenReturnsUnauthorized() throws Exception {
        mvc.perform(get("/audit-logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Given unknown outcome, when list, then returns unprocessable")
    void givenUnknownOutcome_whenList_thenReturnsUnprocessable() throws Exception {
        mvc.perform(get("/audit-logs")
                        .header("Authorization", bearer("ROLE_ADMIN"))
                        .param("outcome", "bogus"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Given invalid pagination, when list, then returns unprocessable")
    void givenInvalidPagination_whenList_thenReturnsUnprocessable() throws Exception {
        mvc.perform(get("/audit-logs")
                        .header("Authorization", bearer("ROLE_ADMIN"))
                        .param("perPage", "999"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Given invalid sort, when list, then returns unprocessable")
    void givenInvalidSort_whenList_thenReturnsUnprocessable() throws Exception {
        mvc.perform(get("/audit-logs")
                        .header("Authorization", bearer("ROLE_ADMIN"))
                        .param("sort", "hackerField"))
                .andExpect(status().isUnprocessableEntity());
    }
}
