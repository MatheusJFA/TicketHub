package com.tickethub.infrastructure.audit;

public enum AuditOutcome {
    SUCCESS,
    NOT_FOUND,
    VALIDATION_ERROR,
    UNAUTHORIZED,
    UNAVAILABLE,
    INFRA_ERROR
}
