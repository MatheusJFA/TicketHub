package com.tickethub.infrastructure.configuration.usecases;

import com.tickethub.application.operator.create.CreateOperatorUseCase;
import com.tickethub.application.operator.create.DefaultCreateOperatorUseCase;
import com.tickethub.domain.authentication.PasswordHasher;
import com.tickethub.domain.core.operator.OperatorGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({OperatorGateway.class})
public class OperatorUseCaseConfig {
    private final OperatorGateway operatorGateway;
    private final PasswordHasher passwordHasher;

    public OperatorUseCaseConfig(final OperatorGateway operatorGateway, final PasswordHasher passwordHasher) {
        this.operatorGateway = operatorGateway;
        this.passwordHasher = passwordHasher;
    }

    @Bean
    public CreateOperatorUseCase createOperatorUseCase() {
        return new DefaultCreateOperatorUseCase(operatorGateway, passwordHasher);
    }
}
