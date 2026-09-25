package com.tickethub.infrastructure.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tickethub.application.Either;
import com.tickethub.application.operator.create.CreateOperatorOutput;
import com.tickethub.application.operator.create.CreateOperatorUseCase;
import com.tickethub.infrastructure.ControllerTest;
import com.tickethub.infrastructure.operator.presenters.OperatorMapperImpl;
import com.tickethub.infrastructure.security.TestTokens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ControllerTest(controllers = OperatorController.class)
@Import({OperatorMapperImpl.class})
@DisplayName("Operator controller")
class OperatorControllerTest {
    @Autowired
    MockMvc mvc;

    @MockitoBean
    CreateOperatorUseCase createOperator;

    @Value("${tickethub.security.jwt.secret}")
    String jwtSecret;

    private String bearer(final String ownerId, final String... authorities) {
        return "Bearer " + TestTokens.bearer(jwtSecret, ownerId, authorities);
    }

    @Test
    @DisplayName("Given admin, when calls create operator, should return operator id")
    void givenAdmin_whenCallsCreateOperator_shouldReturnOperatorId() throws Exception {
        when(createOperator.execute(any())).thenReturn(Either.right(new CreateOperatorOutput("operator-1")));

        mvc.perform(post("/operators")
                        .header("Authorization", bearer(null, "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"name":"Master","email":"master@tickethub.local","password":"secret-123"}
                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/operators/operator-1"))
                .andExpect(jsonPath("$.id").value("operator-1"));
    }

    @Test
    @DisplayName("Given anonymous, when calls create operator, then returns unauthorized")
    void givenAnonymous_whenCallsCreateOperator_thenReturnsUnauthorized() throws Exception {
        mvc.perform(post("/operators").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Master","email":"master@tickethub.local","password":"secret-123"}
                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Given non admin, when calls create operator, then returns forbidden")
    void givenNonAdmin_whenCallsCreateOperator_thenReturnsForbidden() throws Exception {
        mvc.perform(post("/operators")
                        .header("Authorization", bearer("customer-1", "ROLE_CUSTOMER", "customer:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"name":"Master","email":"master@tickethub.local","password":"secret-123"}
                """))
                .andExpect(status().isForbidden());
    }
}
