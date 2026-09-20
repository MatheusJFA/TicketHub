package com.tickethub.infrastructure.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tickethub.application.Either;
import com.tickethub.application.cep.lookup.LookupCepOutput;
import com.tickethub.application.cep.lookup.LookupCepUseCase;
import com.tickethub.domain.validation.Notification;
import com.tickethub.infrastructure.ControllerTest;

@ControllerTest(controllers = CepController.class)
@Import({})
@DisplayName("CEP controller")
class CepControllerTest {
    @Autowired MockMvc mvc;

    @MockitoBean LookupCepUseCase lookupCep;

    @Test
    @DisplayName("Given known zip, when lookup, then returns address without authentication")
    void givenKnownZip_whenLookup_thenReturnsAddress() throws Exception {
        when(lookupCep.execute("01305-000")).thenReturn(Either.right(
                new LookupCepOutput("01305000", "Avenida Paulista", "Bela Vista",
                        "São Paulo", "SP", "Brasil")));

        mvc.perform(get("/cep/01305-000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zipCode").value("01305000"))
                .andExpect(jsonPath("$.street").value("Avenida Paulista"))
                .andExpect(jsonPath("$.city").value("São Paulo"));
    }

    @Test
    @DisplayName("Given unknown zip, when lookup, then returns not found")
    void givenUnknownZip_whenLookup_thenReturnsNotFound() throws Exception {
        when(lookupCep.execute(any())).thenReturn(Either.left(
                Notification.create(new com.tickethub.domain.validation.Error("CepAddress not found: 99999999"))));

        mvc.perform(get("/cep/99999-999"))
                .andExpect(status().isNotFound());
    }
}
