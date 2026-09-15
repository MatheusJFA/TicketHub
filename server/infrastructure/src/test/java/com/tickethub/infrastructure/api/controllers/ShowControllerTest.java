package com.tickethub.infrastructure.api.controllers;

import com.tickethub.application.Either;
import com.tickethub.application.show.addsection.*;
import com.tickethub.application.show.changedescription.*;
import com.tickethub.application.show.changename.*;
import com.tickethub.application.show.create.*;
import com.tickethub.application.show.delete.*;
import com.tickethub.application.show.publish.*;
import com.tickethub.application.show.publishall.*;
import com.tickethub.application.show.reschedule.*;
import com.tickethub.application.show.retrieve.get.*;
import com.tickethub.application.show.retrieve.list.*;
import com.tickethub.application.show.unpublish.*;
import com.tickethub.application.show.unpublishall.*;
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

@ControllerTest(controllers = ShowController.class)
class ShowControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean AddSectionToShowUseCase addSectionToShow;
    @MockitoBean ChangeShowDescriptionUseCase changeShowDescription;
    @MockitoBean ChangeShowNameUseCase changeShowName;
    @MockitoBean CreateShowUseCase createShow;
    @MockitoBean DeleteShowUseCase deleteShow;
    @MockitoBean PublishShowUseCase publishShow;
    @MockitoBean PublishAllShowUseCase publishAllShow;
    @MockitoBean RescheduleShowUseCase rescheduleShow;
    @MockitoBean GetShowUseCase getShow;
    @MockitoBean ListShowsUseCase listShows;
    @MockitoBean UnpublishShowUseCase unpublishShow;
    @MockitoBean UnpublishAllShowUseCase unpublishAllShow;

    @Test
    void givenAValidCommand_whenCallsCreateShow_shouldReturnShowId() throws Exception {
        when(createShow.execute(any())).thenReturn(Either.right(new CreateShowOutput("show-1")));

        mvc.perform(post("/shows").contentType(MediaType.APPLICATION_JSON).content("""
                {"partnerId":"partner-1","name":"Show","description":"Concert","date":"2027-01-15T20:00:00-03:00"}
                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/shows/show-1"))
                .andExpect(jsonPath("$.id").value("show-1"));
    }
}
