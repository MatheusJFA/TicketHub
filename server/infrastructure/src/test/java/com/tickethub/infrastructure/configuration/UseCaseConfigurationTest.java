package com.tickethub.infrastructure.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

import com.tickethub.application.customer.create.CreateCustomerCommand;
import com.tickethub.application.customer.create.CreateCustomerUseCase;
import com.tickethub.application.show.create.CreateShowUseCase;
import com.tickethub.domain.authentication.PasswordHasher;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.infrastructure.configuration.usecases.CustomerUseCaseConfig;
import com.tickethub.infrastructure.configuration.usecases.PartnerUseCaseConfig;
import com.tickethub.infrastructure.configuration.usecases.SectionUseCaseConfig;
import com.tickethub.infrastructure.configuration.usecases.ShowUseCaseConfig;
import com.tickethub.infrastructure.configuration.usecases.SpotUseCaseConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@DisplayName("Use case configuration")
class UseCaseConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(
                    EventConfiguration.class,
                    CustomerUseCaseConfig.class,
                    PartnerUseCaseConfig.class,
                    ShowUseCaseConfig.class,
                    SectionUseCaseConfig.class,
                    SpotUseCaseConfig.class);

    @Test
    @DisplayName("Starts without persistence adapters")
    void startsWithoutPersistenceAdapters() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(CreateCustomerUseCase.class);
            assertThat(context).doesNotHaveBean(CreateShowUseCase.class);
        });
    }

    @Test
    @DisplayName("Wires real use case when gateway exists")
    void wiresRealUseCaseWhenGatewayExists() {
        final var gateway = mock(CustomerGateway.class);
        when(gateway.create(any())).thenAnswer(returnsFirstArg());
        final var passwordHasher = mock(PasswordHasher.class);
        when(passwordHasher.hash(any())).thenReturn("$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS");
        runner.withBean(CustomerGateway.class, () -> gateway)
                .withBean(PasswordHasher.class, () -> passwordHasher)
                .run(context -> {
                    assertThat(context).hasSingleBean(CreateCustomerUseCase.class);
                    final var result = context.getBean(CreateCustomerUseCase.class)
                            .execute(new CreateCustomerCommand(
                                    "52998224725", "Maria", "maria@domain.com", "secret-123"));
                    assertThat(result.isRight()).isTrue();
                    verify(gateway).create(any());
                });
    }

    @Test
    @DisplayName("Show creation requires both gateways")
    void showCreationRequiresBothGateways() {
        runner.withBean(PasswordHasher.class, () -> mock(PasswordHasher.class))
                .withBean(ShowGateway.class, () -> mock(ShowGateway.class))
                .run(context -> assertThat(context).doesNotHaveBean(CreateShowUseCase.class));
        runner.withBean(PasswordHasher.class, () -> mock(PasswordHasher.class))
                .withBean(ShowGateway.class, () -> mock(ShowGateway.class))
                .withBean(PartnerGateway.class, () -> mock(PartnerGateway.class))
                .run(context -> assertThat(context).hasSingleBean(CreateShowUseCase.class));
    }
}
