package com.tickethub.infrastructure.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.tickethub.infrastructure.audit.AuditTrail;
import com.tickethub.infrastructure.audit.MongoAuditTrail;
import com.tickethub.infrastructure.web.CorrelationIdFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.core.MongoTemplate;

@DisplayName("Audit configuration")
class AuditConfigurationTest {

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withUserConfiguration(AuditConfiguration.class);

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("Wires auditor aware")
    void wiresAuditorAware() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(AuditorAware.class);
        });
    }

    @Test
    @DisplayName("Given no actor in mdc, when get current auditor, then returns system")
    void givenNoActorInMdc_whenGetCurrentAuditor_thenReturnsSystem() {
        runner.run(context -> {
            final var auditor = context.getBean("auditorAware", AuditorAware.class);
            assertThat(auditor.getCurrentAuditor()).contains(CorrelationIdFilter.ANONYMOUS_ACTOR);
        });
    }

    @Test
    @DisplayName("Given actor in mdc, when get current auditor, then returns actor")
    void givenActorInMdc_whenGetCurrentAuditor_thenReturnsActor() {
        MDC.put(CorrelationIdFilter.ACTOR_KEY, "alice");

        runner.run(context -> {
            final var auditor = context.getBean("auditorAware", AuditorAware.class);
            assertThat(auditor.getCurrentAuditor()).contains("alice");
        });
    }

    @Test
    @DisplayName("Given Mongo template, when run, then wires Mongo trail")
    void givenMongoTemplate_whenRun_thenWiresMongoTrail() {
        runner.withUserConfiguration(AuditConfiguration.class, MongoAuditTrail.class)
                .withBean(MongoTemplate.class, () -> mock(MongoTemplate.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(MongoAuditTrail.class);
                    assertThat(context).hasSingleBean(AuditTrail.class);
                });
    }

    @Test
    @DisplayName("Given audit disabled, when run, then skips Mongo trail")
    void givenAuditDisabled_whenRun_thenSkipsMongoTrail() {
        runner.withUserConfiguration(AuditConfiguration.class, MongoAuditTrail.class)
                .withBean(MongoTemplate.class, () -> mock(MongoTemplate.class))
                .withPropertyValues("tickethub.audit.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(MongoAuditTrail.class);
                });
    }
}
