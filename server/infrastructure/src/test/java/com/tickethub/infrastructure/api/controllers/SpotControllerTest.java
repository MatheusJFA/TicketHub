package com.tickethub.infrastructure.api.controllers;

import com.tickethub.application.Either;
import com.tickethub.application.spot.changelocation.*;
import com.tickethub.application.spot.create.*;
import com.tickethub.application.spot.delete.*;
import com.tickethub.application.spot.publish.*;
import com.tickethub.application.spot.retrieve.get.*;
import com.tickethub.application.spot.retrieve.list.*;
import com.tickethub.application.spot.unpublish.*;
import com.tickethub.infrastructure.ControllerTest;
import com.tickethub.infrastructure.security.ShowAccess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import com.tickethub.infrastructure.mapping.SharedMapperImpl;
import com.tickethub.infrastructure.mapping.SpotMapperImpl;
import com.tickethub.infrastructure.security.TestTokens;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(controllers = SpotController.class)
@Import({SharedMapperImpl.class, SpotMapperImpl.class})
class SpotControllerTest {
    @Autowired MockMvc mvc;
    @Value("${tickethub.security.jwt.secret}")
    String jwtSecret;

    private String bearer(final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, authorities);
    }

    private String bearerAsOwner(final String ownerId, final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, ownerId, authorities);
    }
    @MockitoBean ChangeSpotLocationUseCase changeSpotLocation;
    @MockitoBean CreateSpotUseCase createSpot;
    @MockitoBean DeleteSpotUseCase deleteSpot;
    @MockitoBean PublishSpotUseCase publishSpot;
    @MockitoBean GetSpotUseCase getSpot;
    @MockitoBean ListSpotsUseCase listSpots;
    @MockitoBean UnpublishSpotUseCase unpublishSpot;
    @MockitoBean(name = "showAccess")
    ShowAccess showAccess;

    @Test
    void givenAValidCommand_whenCallsCreateSpot_shouldReturnSpotId() throws Exception {
        when(createSpot.execute(any())).thenReturn(Either.right(new CreateSpotOutput("spot-1")));

        mvc.perform(post("/spots").header("Authorization", bearer("spot:write")).contentType(MediaType.APPLICATION_JSON).content("{\"location\":\"A1\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/spots/spot-1"))
                .andExpect(jsonPath("$.id").value("spot-1"));
    }

    @Test
    void givenNonOwner_whenChangesSpotLocation_thenReturnsForbidden() throws Exception {
        when(showAccess.canWriteSpot("spot-1")).thenReturn(false);

        mvc.perform(patch("/spots/spot-1/location")
                        .header("Authorization", bearerAsOwner("partner-9", "spot:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"B2\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenOwner_whenChangesSpotLocation_thenSucceeds() throws Exception {
        when(showAccess.canWriteSpot("spot-1")).thenReturn(true);
        when(changeSpotLocation.execute(any()))
                .thenReturn(Either.right(new ChangeSpotLocationOutput("spot-1")));

        mvc.perform(patch("/spots/spot-1/location")
                        .header("Authorization", bearerAsOwner("partner-1", "spot:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"B2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("spot-1"));
    }
}
