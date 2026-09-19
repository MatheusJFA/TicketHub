package com.tickethub.infrastructure.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tickethub.application.Either;
import com.tickethub.application.auth.login.LoginOutput;
import com.tickethub.application.auth.login.LoginUseCase;
import com.tickethub.application.auth.logout.LogoutUseCase;
import com.tickethub.application.auth.refresh.RefreshTokenOutput;
import com.tickethub.application.auth.refresh.RefreshTokenUseCase;
import com.tickethub.domain.auth.AuthenticationException;
import com.tickethub.domain.validation.Notification;
import com.tickethub.infrastructure.ControllerTest;

import java.util.Optional;

@ControllerTest(controllers = AuthController.class)
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
    void givenValidCredentials_whenLogin_thenReturnsSession() throws Exception {
        when(loginUseCase.execute(any()))
                .thenReturn(Either.right(new LoginOutput("access-token", "Bearer", 900, "refresh-token")));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"maria@domain.com\",\"password\":\"secret-123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void givenInvalidCredentials_whenLogin_thenReturns401() throws Exception {
        when(loginUseCase.execute(any()))
                .thenReturn(Either.left(Notification.create(new AuthenticationException("Invalid credentials"))));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"maria@domain.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors[0].message").value("Invalid credentials"));
    }

    @Test
    void givenBlankIdentifier_whenLogin_thenReturns400() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"\",\"password\":\"secret-123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidRefreshToken_whenRefresh_thenReturnsNewSession() throws Exception {
        when(refreshTokenUseCase.execute(any())).thenReturn(
                Either.right(new RefreshTokenOutput("new-access", "Bearer", 900, "new-refresh")));

        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"old-refresh\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    void givenInvalidRefreshToken_whenRefresh_thenReturns401() throws Exception {
        when(refreshTokenUseCase.execute(any())).thenReturn(
                Either.left(Notification.create(new AuthenticationException("Invalid refresh token"))));

        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"unknown\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors[0].message").value("Invalid refresh token"));
    }

    @Test
    void givenRefreshToken_whenLogout_thenReturns204() throws Exception {
        when(logoutUseCase.execute(any())).thenReturn(Optional.empty());

        mvc.perform(post("/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh-token\"}"))
                .andExpect(status().isNoContent());

        verify(logoutUseCase).execute(any());
    }
}
