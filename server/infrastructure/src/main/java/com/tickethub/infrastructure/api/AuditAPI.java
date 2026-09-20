package com.tickethub.infrastructure.api;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tickethub.domain.pagination.Pagination;
import com.tickethub.infrastructure.audit.AuditLogResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/audit-logs", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Audit")
public interface AuditAPI {

    @GetMapping
    @Operation(summary = "List audit trail entries")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns a page of audit entries matching the filters, with pagination metadata"),
            @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid credentials"),
            @ApiResponse(responseCode = "403", description = "The authenticated principal lacks permission for this operation"),
            @ApiResponse(responseCode = "422", description = "Invalid pagination, sorting, outcome or date parameters"),
            @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation")
    })
    @PreAuthorize("hasRole('ADMIN')")
    Pagination<AuditLogResponse> list(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) String correlationId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "occurredAt") String sort,
            @RequestParam(name = "dir", defaultValue = "desc") String direction);
}
