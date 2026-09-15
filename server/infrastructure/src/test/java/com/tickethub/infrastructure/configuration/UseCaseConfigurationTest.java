package com.tickethub.infrastructure.configuration;

import com.tickethub.application.customer.create.CreateCustomerUseCase;
import com.tickethub.application.show.create.CreateShowUseCase;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.show.ShowGateway;
import org.junit.jupiter.api.Test;
import com.tickethub.infrastructure.configuration.usecases.CustomerUseCaseConfig;
import com.tickethub.infrastructure.configuration.usecases.PartnerUseCaseConfig;
import com.tickethub.infrastructure.configuration.usecases.ShowUseCaseConfig;
import com.tickethub.infrastructure.configuration.usecases.SectionUseCaseConfig;
import com.tickethub.infrastructure.configuration.usecases.SpotUseCaseConfig;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

class UseCaseConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(CustomerUseCaseConfig.class, PartnerUseCaseConfig.class,
                    ShowUseCaseConfig.class, SectionUseCaseConfig.class, SpotUseCaseConfig.class);

    @Test
    void startsWithoutPersistenceAdapters() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(CreateCustomerUseCase.class);
            assertThat(context).doesNotHaveBean(CreateShowUseCase.class);
        });
    }

    @Test
    void wiresRealUseCaseWhenGatewayExists() {
        final var gateway = mock(CustomerGateway.class);
        when(gateway.create(any())).thenAnswer(returnsFirstArg());
        runner.withBean(CustomerGateway.class, () -> gateway).run(context -> {
            assertThat(context).hasSingleBean(CreateCustomerUseCase.class);
            final var result = context.getBean(CreateCustomerUseCase.class).execute(
                    new com.tickethub.application.customer.create.CreateCustomerCommand("52998224725", "Maria"));
            assertThat(result.isRight()).isTrue();
            verify(gateway).create(any());
        });
    }

    @Test
    void showCreationRequiresBothGateways() {
        runner.withBean(ShowGateway.class, () -> mock(ShowGateway.class))
                .run(context -> assertThat(context).doesNotHaveBean(CreateShowUseCase.class));
        runner.withBean(ShowGateway.class, () -> mock(ShowGateway.class))
                .withBean(PartnerGateway.class, () -> mock(PartnerGateway.class))
                .run(context -> assertThat(context).hasSingleBean(CreateShowUseCase.class));
    }
}
