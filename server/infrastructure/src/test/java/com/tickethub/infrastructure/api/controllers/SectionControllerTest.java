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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(controllers = SectionController.class)
class SectionControllerTest {
    @Autowired MockMvc mvc;
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

    @Test
    void givenAValidCommand_whenCallsCreateSection_shouldReturnSectionId() throws Exception {
        when(createSection.execute(any())).thenReturn(Either.right(new CreateSectionOutput("section-1")));

        mvc.perform(post("/sections").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"VIP\",\"description\":\"VIP\",\"totalSpots\":10,\"price\":{\"value\":50.00,\"currency\":\"BRL\"}}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/sections/section-1"))
                .andExpect(jsonPath("$.id").value("section-1"));
    }
}
