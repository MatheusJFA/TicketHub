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
import com.tickethub.application.show.update.*;
import com.tickethub.infrastructure.ControllerTest;
import com.tickethub.infrastructure.security.ShowAccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import com.tickethub.infrastructure.shared.presenters.SharedMapperImpl;
import com.tickethub.infrastructure.show.presenters.ShowMapperImpl;
import com.tickethub.infrastructure.security.TestTokens;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(controllers = ShowController.class)
@Import({SharedMapperImpl.class, ShowMapperImpl.class})
@DisplayName("Show controller")
class ShowControllerTest {
    @Autowired MockMvc mvc;
    @Value("${tickethub.security.jwt.secret}")
    String jwtSecret;

    private String bearer(final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, "partner-1", authorities);
    }
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
    @MockitoBean UpdateShowUseCase updateShow;
    @MockitoBean(name = "showAccess") ShowAccess showAccess;

    @Test
    @DisplayName("Given a valid command, when calls create show, should return show id")
    void givenAValidCommand_whenCallsCreateShow_shouldReturnShowId() throws Exception {
        when(createShow.execute(any())).thenReturn(Either.right(new CreateShowOutput("show-1")));
        when(showAccess.canCreate("partner-1")).thenReturn(true);

        mvc.perform(post("/shows").header("Authorization", bearer("show:create")).contentType(MediaType.APPLICATION_JSON).content("""
                {"partnerId":"partner-1","name":"Show","description":"Concert","date":"2027-01-15T20:00:00-03:00"}
                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/shows/show-1"))
                .andExpect(jsonPath("$.id").value("show-1"));
    }

    @Test
    @DisplayName("Given owner, when calls update show, then succeeds")
    void givenOwner_whenCallsUpdateShow_thenSucceeds() throws Exception {
        when(showAccess.canWrite("show-1")).thenReturn(true);
        when(updateShow.execute(any()))
                .thenReturn(Either.right(new UpdateShowOutput("show-1")));

        mvc.perform(put("/shows/show-1").header("Authorization", bearer("show:write")).contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Show","description":"Festival","date":"2027-02-20T21:00:00-03:00"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("show-1"));
    }

    @Test
    @DisplayName("Given non owner, when calls update show, then returns forbidden")
    void givenNonOwner_whenCallsUpdateShow_thenReturnsForbidden() throws Exception {
        when(showAccess.canWrite("show-1")).thenReturn(false);

        mvc.perform(put("/shows/show-1").header("Authorization", bearer("show:write")).contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Show","description":"Festival","date":"2027-02-20T21:00:00-03:00"}
                """))
                .andExpect(status().isForbidden());
    }
}
