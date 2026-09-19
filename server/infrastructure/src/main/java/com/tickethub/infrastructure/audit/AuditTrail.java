package com.tickethub.infrastructure.audit;

import java.util.Objects;

public interface AuditTrail {

    void record(AuditEntry entry);

    static AuditTrail noop() {
        return entry -> Objects.requireNonNull(entry, "'entry' should not be null");
    }
}
