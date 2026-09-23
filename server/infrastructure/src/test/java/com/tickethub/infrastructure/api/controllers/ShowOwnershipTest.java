package com.tickethub.infrastructure.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tickethub.application.Either;
import com.tickethub.application.section.retrieve.byshow.*;
import com.tickethub.application.show.addsection.AddSectionToShowUseCase;
import com.tickethub.application.show.changedescription.ChangeShowDescriptionUseCase;
import com.tickethub.application.show.changename.ChangeShowNameUseCase;
import com.tickethub.application.show.create.CreateShowUseCase;
import com.tickethub.application.show.delete.DeleteShowUseCase;
import com.tickethub.application.show.publish.PublishShowOutput;
import com.tickethub.application.show.publish.PublishShowUseCase;
import com.tickethub.application.show.publishall.PublishAllShowUseCase;
import com.tickethub.application.show.reschedule.RescheduleShowUseCase;
import com.tickethub.application.show.retrieve.get.GetShowUseCase;
import com.tickethub.application.show.retrieve.list.ListShowsUseCase;
import com.tickethub.application.show.unpublish.UnpublishShowUseCase;
import com.tickethub.application.show.unpublishall.UnpublishAllShowUseCase;
import com.tickethub.application.show.update.*;
import com.tickethub.infrastructure.ControllerTest;
import com.tickethub.infrastructure.section.presenters.SectionMapperImpl;
import com.tickethub.infrastructure.security.ShowAccess;
import com.tickethub.infrastructure.security.TestTokens;
import com.tickethub.infrastructure.shared.presenters.SharedMapperImpl;
import com.tickethub.infrastructure.show.presenters.ShowMapperImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ControllerTest(controllers = ShowController.class)
@Import({SharedMapperImpl.class, ShowMapperImpl.class, SectionMapperImpl.class})
@DisplayName("Show ownership")
class ShowOwnershipTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AddSectionToShowUseCase addSectionToShow;

    @MockitoBean
    ChangeShowDescriptionUseCase changeShowDescription;

    @MockitoBean
    ChangeShowNameUseCase changeShowName;

    @MockitoBean
    CreateShowUseCase createShow;

    @MockitoBean
    DeleteShowUseCase deleteShow;

    @MockitoBean
    PublishShowUseCase publishShow;

    @MockitoBean
    PublishAllShowUseCase publishAllShow;

    @MockitoBean
    RescheduleShowUseCase rescheduleShow;

    @MockitoBean
    GetShowUseCase getShow;

    @MockitoBean
    ListShowsUseCase listShows;

    @MockitoBean
    UnpublishShowUseCase unpublishShow;

    @MockitoBean
    UnpublishAllShowUseCase unpublishAllShow;

    @MockitoBean
    UpdateShowUseCase updateShow;

    @MockitoBean
    ListShowSectionsUseCase listShowSections;

    @MockitoBean(name = "showAccess")
    ShowAccess showAccess;

    @Value("${tickethub.security.jwt.secret}")
    String jwtSecret;

    private String bearer(final String ownerId, final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, ownerId, authorities);
    }

    @Test
    @DisplayName("Given no token, when publish show, then returns401")
    void givenNoToken_whenPublishShow_thenReturns401() throws Exception {
        mvc.perform(post("/shows/show-1/publish")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Given owner, when publish show, then succeeds")
    void givenOwner_whenPublishShow_thenSucceeds() throws Exception {
        when(publishShow.execute(any())).thenReturn(Either.right(PublishShowOutput.from("show-1")));
        when(showAccess.canPublish("show-1")).thenReturn(true);

        mvc.perform(post("/shows/show-1/publish").header("Authorization", bearer("partner-1", "show:publish")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("show-1"));
    }

    @Test
    @DisplayName("Given another partner, when publish show, then returns forbidden")
    void givenAnotherPartner_whenPublishShow_thenReturnsForbidden() throws Exception {
        when(showAccess.canPublish("show-1")).thenReturn(false);

        mvc.perform(post("/shows/show-1/publish").header("Authorization", bearer("partner-9", "show:publish")))
                .andExpect(status().isForbidden());
    }
}
