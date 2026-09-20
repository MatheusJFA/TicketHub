package com.tickethub.infrastructure.audit;

import com.tickethub.domain.pagination.Pagination;

/**
 * Reads the audit trail persisted by the {@code AuditTrail} port.
 */
public interface AuditLogReader {

    Pagination<AuditLogResponse> search(AuditLogQuery query);
}
