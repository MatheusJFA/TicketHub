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
import com.tickethub.application.zipcode.lookup.LookupZipCodeOutput;
import com.tickethub.application.zipcode.lookup.LookupZipCodeUseCase;
import com.tickethub.domain.validation.Notification;
import com.tickethub.infrastructure.ControllerTest;

@ControllerTest(controllers = ZipCodeController.class)
@Import({})
@DisplayName("ZIP code controller")
class ZipCodeControllerTest {
    @Autowired MockMvc mvc;

    @MockitoBean LookupZipCodeUseCase lookupZipCode;

    @Test
    @DisplayName("Given known zip, when lookup, then returns address without authentication")
    void givenKnownZip_whenLookup_thenReturnsAddress() throws Exception {
        when(lookupZipCode.execute("01305-000")).thenReturn(Either.right(
                new LookupZipCodeOutput("01305000", "Avenida Paulista", "Bela Vista",
                        "São Paulo", "SP", "Brasil")));

        mvc.perform(get("/zipcode/01305-000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zipCode").value("01305000"))
                .andExpect(jsonPath("$.street").value("Avenida Paulista"))
                .andExpect(jsonPath("$.city").value("São Paulo"));
    }

    @Test
    @DisplayName("Given unknown zip, when lookup, then returns not found")
    void givenUnknownZip_whenLookup_thenReturnsNotFound() throws Exception {
        when(lookupZipCode.execute(any())).thenReturn(Either.left(
                Notification.create(new com.tickethub.domain.validation.Error("ZipCodeAddress not found: 99999999"))));

        mvc.perform(get("/zipcode/99999-999"))
                .andExpect(status().isNotFound());
    }
}
