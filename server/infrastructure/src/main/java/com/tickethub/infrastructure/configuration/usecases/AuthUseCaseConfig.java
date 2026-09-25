package com.tickethub.infrastructure.configuration.usecases;

import com.tickethub.application.authentication.login.DefaultLoginUseCase;
import com.tickethub.application.authentication.login.LoginUseCase;
import com.tickethub.application.authentication.logout.DefaultLogoutUseCase;
import com.tickethub.application.authentication.logout.LogoutUseCase;
import com.tickethub.application.authentication.refresh.DefaultRefreshTokenUseCase;
import com.tickethub.application.authentication.refresh.RefreshTokenUseCase;
import com.tickethub.domain.authentication.AccessTokenInspector;
import com.tickethub.domain.authentication.AuthAccountGateway;
import com.tickethub.domain.authentication.PasswordHasher;
import com.tickethub.domain.authentication.RefreshSessionGateway;
import com.tickethub.domain.authentication.RevokedAccessTokenGateway;
import com.tickethub.domain.authentication.TokenIssuer;
import com.tickethub.infrastructure.authentication.AuthSessionProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({AuthAccountGateway.class, RefreshSessionGateway.class})
public class AuthUseCaseConfig {

    @Bean
    public LoginUseCase loginUseCase(
            final AuthAccountGateway authAccounts,
            final PasswordHasher passwordHasher,
            final TokenIssuer tokenIssuer,
            final RefreshSessionGateway refreshSessions,
            final AuthSessionProperties properties) {
        return new DefaultLoginUseCase(
                authAccounts, passwordHasher, tokenIssuer, refreshSessions, properties.getRefreshTtl());
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(
            final TokenIssuer tokenIssuer,
            final RefreshSessionGateway refreshSessions,
            final AuthSessionProperties properties) {
        return new DefaultRefreshTokenUseCase(tokenIssuer, refreshSessions, properties.getRefreshTtl());
    }

    @Bean
    public LogoutUseCase logoutUseCase(
            final RefreshSessionGateway refreshSessions,
            final RevokedAccessTokenGateway revokedAccessTokens,
            final AccessTokenInspector accessTokenInspector) {
        return new DefaultLogoutUseCase(refreshSessions, revokedAccessTokens, accessTokenInspector);
    }
}
