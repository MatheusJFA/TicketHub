package com.tickethub.infrastructure.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.tickethub.infrastructure.audit.AuditTrail;
import com.tickethub.infrastructure.audit.MongoAuditTrail;
import com.tickethub.infrastructure.web.CorrelationIdFilter;

class AuditConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(AuditConfiguration.class);

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void wiresAuditorAware() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(AuditorAware.class);
        });
    }

    @Test
    void givenNoActorInMdc_whenGetCurrentAuditor_thenReturnsSystem() {
        runner.run(context -> {
            final var auditor = context.getBean("auditorAware", AuditorAware.class);
            assertThat(auditor.getCurrentAuditor()).contains(CorrelationIdFilter.ANONYMOUS_ACTOR);
        });
    }

    @Test
    void givenActorInMdc_whenGetCurrentAuditor_thenReturnsActor() {
        MDC.put(CorrelationIdFilter.ACTOR_KEY, "alice");

        runner.run(context -> {
            final var auditor = context.getBean("auditorAware", AuditorAware.class);
            assertThat(auditor.getCurrentAuditor()).contains("alice");
        });
    }

    @Test
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
