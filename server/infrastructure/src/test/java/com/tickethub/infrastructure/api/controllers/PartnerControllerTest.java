package com.tickethub.infrastructure.api.controllers;

import com.tickethub.application.Either;
import com.tickethub.application.partner.changeaddress.*;
import com.tickethub.application.partner.changename.*;
import com.tickethub.application.partner.create.*;
import com.tickethub.application.partner.delete.*;
import com.tickethub.application.partner.retrieve.get.*;
import com.tickethub.application.partner.retrieve.list.*;
import com.tickethub.infrastructure.ControllerTest;
import com.tickethub.infrastructure.security.OwnerAccess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.tickethub.infrastructure.security.TestTokens;
import org.springframework.beans.factory.annotation.Value;

@ControllerTest(controllers = PartnerController.class)
class PartnerControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean ChangePartnerAddressUseCase changePartnerAddress;
    @MockitoBean ChangePartnerNameUseCase changePartnerName;
    @MockitoBean CreatePartnerUseCase createPartner;
    @MockitoBean DeletePartnerUseCase deletePartner;
    @MockitoBean GetPartnerUseCase getPartner;
    @MockitoBean ListPartnersUseCase listPartners;
    @MockitoBean(name = "ownerAccess") OwnerAccess ownerAccess;

    @Value("${tickethub.security.jwt.secret}")
    String jwtSecret;

    private String bearer(final String ownerId, final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, ownerId, authorities);
    }

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

    @Test
    void givenOwner_whenCallsDeleteOwnPartner_thenReturnsNoContent() throws Exception {
        when(deletePartner.execute("partner-1")).thenReturn(Either.right(new DeletePartnerOutput("partner-1")));
        when(ownerAccess.isSelfOrAdmin("partner-1")).thenReturn(true);

        mvc.perform(delete("/partners/partner-1")
                        .header("Authorization", bearer("partner-1", "partner:delete")))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenAnotherAccount_whenCallsDeletePartner_thenReturnsForbidden() throws Exception {
        mvc.perform(delete("/partners/partner-1")
                        .header("Authorization", bearer("partner-9", "partner:delete")))
                .andExpect(status().isForbidden());
    }
}
