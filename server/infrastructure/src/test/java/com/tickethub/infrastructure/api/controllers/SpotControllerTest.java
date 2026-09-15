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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(controllers = SpotController.class)
class SpotControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean ChangeSpotLocationUseCase changeSpotLocation;
    @MockitoBean CreateSpotUseCase createSpot;
    @MockitoBean DeleteSpotUseCase deleteSpot;
    @MockitoBean PublishSpotUseCase publishSpot;
    @MockitoBean GetSpotUseCase getSpot;
    @MockitoBean ListSpotsUseCase listSpots;
    @MockitoBean UnpublishSpotUseCase unpublishSpot;

    @Test
    void givenAValidCommand_whenCallsCreateSpot_shouldReturnSpotId() throws Exception {
        when(createSpot.execute(any())).thenReturn(Either.right(new CreateSpotOutput("spot-1")));

        mvc.perform(post("/spots").contentType(MediaType.APPLICATION_JSON).content("{\"location\":\"A1\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/spots/spot-1"))
                .andExpect(jsonPath("$.id").value("spot-1"));
    }
}
