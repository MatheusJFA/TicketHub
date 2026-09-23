package com.tickethub.infrastructure.audit;

import com.tickethub.infrastructure.web.CorrelationIdFilter;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

/**
 * Resolves the actor responsible for a write: the authenticated principal
 * (JWT {@code sub}) propagated by the {@code CorrelationIdFilter}, falling
 * back to the {@code X-Actor} header and finally to the anonymous actor.
 */
public final class AuditActor {

    private AuditActor() {}

    public static Optional<String> current() {
        return Optional.ofNullable(MDC.get(CorrelationIdFilter.ACTOR_KEY)).filter(StringUtils::isNotBlank);
    }

    public static String currentOrAnonymous() {
        return current().orElse(CorrelationIdFilter.ANONYMOUS_ACTOR);
    }
}
