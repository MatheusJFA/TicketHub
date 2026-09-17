package com.tickethub.infrastructure.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tickethub.application.Either;
import com.tickethub.application.spot.changelocation.ChangeSpotLocationUseCase;
import com.tickethub.application.spot.create.CreateSpotOutput;
import com.tickethub.application.spot.create.CreateSpotUseCase;
import com.tickethub.application.spot.delete.DeleteSpotUseCase;
import com.tickethub.application.spot.publish.PublishSpotUseCase;
import com.tickethub.application.spot.retrieve.get.GetSpotUseCase;
import com.tickethub.application.spot.retrieve.list.ListSpotsUseCase;
import com.tickethub.application.spot.unpublish.UnpublishSpotUseCase;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.infrastructure.ControllerTest;
import com.tickethub.infrastructure.security.TestTokens;

@ControllerTest(controllers = SpotController.class)
class SecurityEnforcementTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ChangeSpotLocationUseCase changeSpotLocation;
    @MockitoBean
    CreateSpotUseCase createSpot;
    @MockitoBean
    DeleteSpotUseCase deleteSpot;
    @MockitoBean
    PublishSpotUseCase publishSpot;
    @MockitoBean
    GetSpotUseCase getSpot;
    @MockitoBean
    ListSpotsUseCase listSpots;
    @MockitoBean
    UnpublishSpotUseCase unpublishSpot;

    @Value("${tickethub.security.jwt.secret}")
    String jwtSecret;

    private String bearer(final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, authorities);
    }

    @Test
    void givenNoToken_whenCallsProtectedEndpoint_thenReturns401() throws Exception {
        mvc.perform(post("/spots").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"A1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenTokenWithoutAuthority_whenCallsProtectedEndpoint_thenReturns403() throws Exception {
        mvc.perform(post("/spots").header("Authorization", bearer("spot:read"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"A1\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errors[0].message").value("Access denied"));
    }

    @Test
    void givenTokenWithAuthority_whenCallsProtectedEndpoint_thenSucceeds() throws Exception {
        when(createSpot.execute(any())).thenReturn(Either.right(new CreateSpotOutput("spot-1")));

        mvc.perform(post("/spots").header("Authorization", bearer("spot:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"A1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("spot-1"));
    }

    @Test
    void givenNoToken_whenCallsPublicCatalog_thenSucceeds() throws Exception {
        when(listSpots.execute(any()))
                .thenReturn(Either.right(new Pagination<>(0, 10, 0, List.of())));

        mvc.perform(get("/spots"))
                .andExpect(status().isOk());
    }
}
