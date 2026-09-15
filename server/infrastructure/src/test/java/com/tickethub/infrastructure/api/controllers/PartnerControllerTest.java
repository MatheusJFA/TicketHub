package com.tickethub.infrastructure.api.controllers;

import com.tickethub.application.Either;
import com.tickethub.application.partner.changeaddress.*;
import com.tickethub.application.partner.changename.*;
import com.tickethub.application.partner.create.*;
import com.tickethub.application.partner.delete.*;
import com.tickethub.application.partner.retrieve.get.*;
import com.tickethub.application.partner.retrieve.list.*;
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

@ControllerTest(controllers = PartnerController.class)
class PartnerControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean ChangePartnerAddressUseCase changePartnerAddress;
    @MockitoBean ChangePartnerNameUseCase changePartnerName;
    @MockitoBean CreatePartnerUseCase createPartner;
    @MockitoBean DeletePartnerUseCase deletePartner;
    @MockitoBean GetPartnerUseCase getPartner;
    @MockitoBean ListPartnersUseCase listPartners;

    @Test
    void givenAValidCommand_whenCallsCreatePartner_shouldReturnPartnerId() throws Exception {
        when(createPartner.execute(any())).thenReturn(Either.right(new CreatePartnerOutput("partner-1")));

        mvc.perform(post("/partners").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Cinema","cnpj":"11222333000181","address":{"street":"Rua A","number":"1","neighborhood":"Centro","city":"São Paulo","state":"SP","country":"Brasil","zipCode":"01001000"}}
                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/partners/partner-1"))
                .andExpect(jsonPath("$.id").value("partner-1"));
    }
}
