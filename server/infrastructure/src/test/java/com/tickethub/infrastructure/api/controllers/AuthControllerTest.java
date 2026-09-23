package com.tickethub.infrastructure.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tickethub.application.Either;
import com.tickethub.application.authentication.login.LoginOutput;
import com.tickethub.application.authentication.login.LoginUseCase;
import com.tickethub.application.authentication.logout.LogoutUseCase;
import com.tickethub.application.authentication.refresh.RefreshTokenOutput;
import com.tickethub.application.authentication.refresh.RefreshTokenUseCase;
import com.tickethub.domain.authentication.AuthenticationException;
import com.tickethub.domain.validation.Notification;
import com.tickethub.infrastructure.ControllerTest;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ControllerTest(controllers = AuthController.class)
@DisplayName("Auth controller")
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    LoginUseCase loginUseCase;

    @MockitoBean
    RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    LogoutUseCase logoutUseCase;

    @Test
    @DisplayName("Given valid credentials, when login, then returns session")
    void givenValidCredentials_whenLogin_thenReturnsSession() throws Exception {
        when(loginUseCase.execute(any()))
                .thenReturn(Either.right(new LoginOutput("access-token", "Bearer", 900, "refresh-token")));

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"maria@domain.com\",\"password\":\"secret-123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("Given invalid credentials, when login, then returns401")
    void givenInvalidCredentials_whenLogin_thenReturns401() throws Exception {
        when(loginUseCase.execute(any()))
                .thenReturn(Either.left(Notification.create(new AuthenticationException("Invalid credentials"))));

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"maria@domain.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors[0].message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("Given blank identifier, when login, then returns400")
    void givenBlankIdentifier_whenLogin_thenReturns400() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"\",\"password\":\"secret-123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Given valid refresh token, when refresh, then returns new session")
    void givenValidRefreshToken_whenRefresh_thenReturnsNewSession() throws Exception {
        when(refreshTokenUseCase.execute(any()))
                .thenReturn(Either.right(new RefreshTokenOutput("new-access", "Bearer", 900, "new-refresh")));

        mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"old-refresh\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    @DisplayName("Given invalid refresh token, when refresh, then returns401")
    void givenInvalidRefreshToken_whenRefresh_thenReturns401() throws Exception {
        when(refreshTokenUseCase.execute(any()))
                .thenReturn(Either.left(Notification.create(new AuthenticationException("Invalid refresh token"))));

        mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"unknown\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors[0].message").value("Invalid refresh token"));
    }

    @Test
    @DisplayName("Given refresh token, when logout, then returns204")
    void givenRefreshToken_whenLogout_thenReturns204() throws Exception {
        when(logoutUseCase.execute(any())).thenReturn(Optional.empty());

        mvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh-token\"}"))
                .andExpect(status().isNoContent());

        verify(logoutUseCase).execute(any());
    }
}
