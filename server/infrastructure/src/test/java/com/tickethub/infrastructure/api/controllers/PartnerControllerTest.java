package com.tickethub.infrastructure.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tickethub.application.Either;
import com.tickethub.application.partner.approve.ApprovePartnerOutput;
import com.tickethub.application.partner.approve.ApprovePartnerUseCase;
import com.tickethub.application.partner.changeaddress.*;
import com.tickethub.application.partner.changename.*;
import com.tickethub.application.partner.changewebhook.ChangePartnerWebhookOutput;
import com.tickethub.application.partner.changewebhook.ChangePartnerWebhookUseCase;
import com.tickethub.application.partner.create.*;
import com.tickethub.application.partner.delete.DeletePartnerUseCase;
import com.tickethub.application.partner.reject.RejectPartnerOutput;
import com.tickethub.application.partner.reject.RejectPartnerUseCase;
import com.tickethub.application.partner.retrieve.get.*;
import com.tickethub.application.partner.retrieve.list.*;
import com.tickethub.application.partner.update.*;
import com.tickethub.infrastructure.ControllerTest;
import com.tickethub.infrastructure.partner.presenters.PartnerMapperImpl;
import com.tickethub.infrastructure.security.OwnerAccess;
import com.tickethub.infrastructure.security.TestTokens;
import com.tickethub.infrastructure.shared.presenters.SharedMapperImpl;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ControllerTest(controllers = PartnerController.class)
@Import({SharedMapperImpl.class, PartnerMapperImpl.class})
@DisplayName("Partner controller")
class PartnerControllerTest {
    @Autowired
    MockMvc mvc;

    @MockitoBean
    ChangePartnerAddressUseCase changePartnerAddress;

    @MockitoBean
    ChangePartnerNameUseCase changePartnerName;

    @MockitoBean
    ChangePartnerWebhookUseCase changePartnerWebhook;

    @MockitoBean
    CreatePartnerUseCase createPartner;

    @MockitoBean
    DeletePartnerUseCase deletePartner;

    @MockitoBean
    ApprovePartnerUseCase approvePartner;

    @MockitoBean
    RejectPartnerUseCase rejectPartner;

    @MockitoBean
    GetPartnerUseCase getPartner;

    @MockitoBean
    ListPartnersUseCase listPartners;

    @MockitoBean
    UpdatePartnerUseCase updatePartner;

    @MockitoBean(name = "ownerAccess")
    OwnerAccess ownerAccess;

    @Value("${tickethub.security.jwt.secret}")
    String jwtSecret;

    private String bearer(final String ownerId, final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, ownerId, authorities);
    }

    @Test
    @DisplayName("Given a registration request, when calls create partner, should return pending partner id")
    void givenRegistrationRequest_whenCallsCreatePartner_shouldReturnPendingPartnerId() throws Exception {
        when(createPartner.execute(any())).thenReturn(Either.right(new CreatePartnerOutput("partner-1")));

        mvc.perform(post("/partners").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Cinema","cnpj":"11222333000181","address":{"street":"Rua A","number":"1","neighborhood":"Centro","city":"São Paulo","state":"SP","country":"Brasil","zipCode":"01001000"},"email":"cinema@domain.com","password":"secret-123"}
                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/partners/partner-1"))
                .andExpect(jsonPath("$.id").value("partner-1"));
    }

    @Test
    @DisplayName("Given admin, when calls approve partner, should return active status")
    void givenAdmin_whenCallsApprovePartner_shouldReturnActiveStatus() throws Exception {
        when(approvePartner.execute("partner-1"))
                .thenReturn(Either.right(new ApprovePartnerOutput("partner-1", "ACTIVE")));

        mvc.perform(post("/partners/partner-1/approve").header("Authorization", bearer(null, "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("partner-1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Given admin, when calls reject partner, should return rejected status")
    void givenAdmin_whenCallsRejectPartner_shouldReturnRejectedStatus() throws Exception {
        when(rejectPartner.execute("partner-1"))
                .thenReturn(Either.right(new RejectPartnerOutput("partner-1", "REJECTED")));

        mvc.perform(post("/partners/partner-1/reject").header("Authorization", bearer(null, "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("partner-1"))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @DisplayName("Given anonymous, when calls approve partner, then returns unauthorized")
    void givenAnonymous_whenCallsApprovePartner_thenReturnsUnauthorized() throws Exception {
        mvc.perform(post("/partners/partner-1/approve")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Given non admin, when calls approve partner, then returns forbidden")
    void givenNonAdmin_whenCallsApprovePartner_thenReturnsForbidden() throws Exception {
        mvc.perform(post("/partners/partner-1/approve")
                        .header("Authorization", bearer("partner-1", "ROLE_PARTNER", "show:create")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Given owner, when calls change webhook, then succeeds")
    void givenOwner_whenCallsChangeWebhook_thenSucceeds() throws Exception {
        when(changePartnerWebhook.execute(any())).thenReturn(Either.right(new ChangePartnerWebhookOutput("partner-1")));
        when(ownerAccess.isSelfOrAdmin("partner-1")).thenReturn(true);

        mvc.perform(put("/partners/partner-1/webhook")
                        .header("Authorization", bearer("partner-1", "partner:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"webhookUrl":"https://partner.domain.com/hook","webhookSecret":"s3cr3t"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("partner-1"));
    }

    @Test
    @DisplayName("Given another account, when calls change webhook, then returns forbidden")
    void givenAnotherAccount_whenCallsChangeWebhook_thenReturnsForbidden() throws Exception {
        mvc.perform(put("/partners/partner-1/webhook")
                        .header("Authorization", bearer("partner-9", "partner:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"webhookUrl":"https://partner.domain.com/hook","webhookSecret":"s3cr3t"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Given owner, when calls delete own partner, then returns no content")
    void givenOwner_whenCallsDeleteOwnPartner_thenReturnsNoContent() throws Exception {
        when(deletePartner.execute("partner-1")).thenReturn(Optional.empty());
        when(ownerAccess.isSelfOrAdmin("partner-1")).thenReturn(true);

        mvc.perform(delete("/partners/partner-1").header("Authorization", bearer("partner-1", "partner:delete")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Given another account, when calls delete partner, then returns forbidden")
    void givenAnotherAccount_whenCallsDeletePartner_thenReturnsForbidden() throws Exception {
        mvc.perform(delete("/partners/partner-1").header("Authorization", bearer("partner-9", "partner:delete")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Given owner, when calls update partner, then succeeds")
    void givenOwner_whenCallsUpdatePartner_thenSucceeds() throws Exception {
        when(updatePartner.execute(any())).thenReturn(Either.right(new UpdatePartnerOutput("partner-1")));
        when(ownerAccess.isSelfOrAdmin("partner-1")).thenReturn(true);

        mvc.perform(put("/partners/partner-1")
                        .header("Authorization", bearer("partner-1", "partner:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cinema Novo","address":{"street":"Rua B","number":"2","neighborhood":"Centro","city":"São Paulo","state":"SP","country":"Brasil","zipCode":"01001000"}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("partner-1"));
    }

    @Test
    @DisplayName("Given another account, when calls update partner, then returns forbidden")
    void givenAnotherAccount_whenCallsUpdatePartner_thenReturnsForbidden() throws Exception {
        mvc.perform(put("/partners/partner-1")
                        .header("Authorization", bearer("partner-9", "partner:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cinema Novo","address":{"street":"Rua B","number":"2","neighborhood":"Centro","city":"São Paulo","state":"SP","country":"Brasil","zipCode":"01001000"}}
                                """))
                .andExpect(status().isForbidden());
    }
}
