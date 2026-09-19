package com.tickethub.infrastructure.configuration.usecases;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tickethub.application.auth.login.DefaultLoginUseCase;
import com.tickethub.application.auth.login.LoginUseCase;
import com.tickethub.application.auth.logout.DefaultLogoutUseCase;
import com.tickethub.application.auth.logout.LogoutUseCase;
import com.tickethub.application.auth.refresh.DefaultRefreshTokenUseCase;
import com.tickethub.application.auth.refresh.RefreshTokenUseCase;
import com.tickethub.domain.auth.AuthAccountGateway;
import com.tickethub.domain.auth.PasswordHasher;
import com.tickethub.domain.auth.RefreshSessionGateway;
import com.tickethub.domain.auth.TokenIssuer;
import com.tickethub.infrastructure.auth.AuthSessionProperties;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({ AuthAccountGateway.class, RefreshSessionGateway.class })
public class AuthUseCaseConfig {

    @Bean
    public LoginUseCase loginUseCase(final AuthAccountGateway authAccounts, final PasswordHasher passwordHasher,
            final TokenIssuer tokenIssuer, final RefreshSessionGateway refreshSessions,
            final AuthSessionProperties properties) {
        return new DefaultLoginUseCase(authAccounts, passwordHasher, tokenIssuer, refreshSessions,
                properties.getRefreshTtl());
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(final TokenIssuer tokenIssuer,
            final RefreshSessionGateway refreshSessions, final AuthSessionProperties properties) {
        return new DefaultRefreshTokenUseCase(tokenIssuer, refreshSessions, properties.getRefreshTtl());
    }

    @Bean
    public LogoutUseCase logoutUseCase(final RefreshSessionGateway refreshSessions) {
        return new DefaultLogoutUseCase(refreshSessions);
    }
}
