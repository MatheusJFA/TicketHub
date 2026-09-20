package com.tickethub.infrastructure.audit;

import static java.util.Objects.requireNonNull;

public interface AuditTrail {

    void record(AuditEntry entry);

    static AuditTrail noop() {
        return entry -> requireNonNull(entry, "'entry' should not be null");
    }
}
