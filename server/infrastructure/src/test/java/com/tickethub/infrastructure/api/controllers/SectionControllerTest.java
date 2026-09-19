package com.tickethub.infrastructure.api.controllers;

import com.tickethub.application.Either;
import com.tickethub.application.section.changedescription.*;
import com.tickethub.application.section.changename.*;
import com.tickethub.application.section.changeprice.*;
import com.tickethub.application.section.create.*;
import com.tickethub.application.section.delete.*;
import com.tickethub.application.section.publish.*;
import com.tickethub.application.section.publishall.*;
import com.tickethub.application.section.retrieve.get.*;
import com.tickethub.application.section.retrieve.list.*;
import com.tickethub.application.section.unpublish.*;
import com.tickethub.application.section.unpublishall.*;
import com.tickethub.infrastructure.ControllerTest;
import com.tickethub.infrastructure.security.ShowAccess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import com.tickethub.infrastructure.shared.presenters.SharedMapperImpl;
import com.tickethub.infrastructure.section.presenters.SectionMapperImpl;
import com.tickethub.infrastructure.security.TestTokens;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(controllers = SectionController.class)
@Import({SharedMapperImpl.class, SectionMapperImpl.class})
class SectionControllerTest {
    @Autowired MockMvc mvc;
    @Value("${tickethub.security.jwt.secret}")
    String jwtSecret;

    private String bearer(final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, authorities);
    }

    private String bearerAsOwner(final String ownerId, final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, ownerId, authorities);
    }
    @MockitoBean ChangeSectionDescriptionUseCase changeSectionDescription;
    @MockitoBean ChangeSectionNameUseCase changeSectionName;
    @MockitoBean ChangeSectionPriceUseCase changeSectionPrice;
    @MockitoBean CreateSectionUseCase createSection;
    @MockitoBean DeleteSectionUseCase deleteSection;
    @MockitoBean PublishSectionUseCase publishSection;
    @MockitoBean PublishAllSectionUseCase publishAllSection;
    @MockitoBean GetSectionUseCase getSection;
    @MockitoBean ListSectionsUseCase listSections;
    @MockitoBean UnpublishSectionUseCase unpublishSection;
    @MockitoBean UnpublishAllSectionUseCase unpublishAllSection;
    @MockitoBean(name = "showAccess")
    ShowAccess showAccess;

    @Test
    void givenAValidCommand_whenCallsCreateSection_shouldReturnSectionId() throws Exception {
        when(createSection.execute(any())).thenReturn(Either.right(new CreateSectionOutput("section-1")));

        mvc.perform(post("/sections").header("Authorization", bearer("section:write")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"VIP\",\"description\":\"VIP\",\"totalSpots\":10,\"price\":{\"value\":50.00,\"currency\":\"BRL\"}}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/sections/section-1"))
                .andExpect(jsonPath("$.id").value("section-1"));
    }

    @Test
    void givenNonOwner_whenChangesSectionName_thenReturnsForbidden() throws Exception {
        when(showAccess.canWriteSection("section-1")).thenReturn(false);

        mvc.perform(patch("/sections/section-1/name")
                        .header("Authorization", bearerAsOwner("partner-9", "section:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pista\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenOwner_whenChangesSectionName_thenSucceeds() throws Exception {
        when(showAccess.canWriteSection("section-1")).thenReturn(true);
        when(changeSectionName.execute(any()))
                .thenReturn(Either.right(new ChangeSectionNameOutput("section-1")));

        mvc.perform(patch("/sections/section-1/name")
                        .header("Authorization", bearerAsOwner("partner-1", "section:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pista\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("section-1"));
    }
}
