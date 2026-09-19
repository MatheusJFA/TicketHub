package com.tickethub.infrastructure.api;

import com.tickethub.infrastructure.api.models.*;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.application.customer.changename.*;
import com.tickethub.application.customer.create.*;
import com.tickethub.application.customer.delete.*;
import com.tickethub.application.customer.retrieve.get.*;
import com.tickethub.application.customer.retrieve.list.*;
import com.tickethub.infrastructure.customer.models.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;

@RequestMapping(value = "/customers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Customers")
public interface CustomerAPI {
    @PatchMapping(value = "/{id}/name", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Change Customer Name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Customer name updated successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('customer:write') and @ownerAccess.isSelfOrAdmin(#id)")
    ResponseEntity<IdResponse> changeCustomerName(@PathVariable("id") String id, @RequestBody ChangeCustomerNameRequest input);

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Customer")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Customer created successfully; returns its identifier and the resource URL in the Location header"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    ResponseEntity<IdResponse> createCustomer(@RequestBody CreateCustomerRequest input);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Customer")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Customer deletion completed successfully; no response body"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('customer:delete') and @ownerAccess.isSelfOrAdmin(#id)")
    ResponseEntity<Void> deleteById(@PathVariable("id") String id);

    @GetMapping("/{id}")
    @Operation(summary = "Get Customer")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Customer found; returns the resource details"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("@ownerAccess.isSelfOrAdmin(#id)")
    ResponseEntity<CustomerResponse> getById(@PathVariable("id") String id);

    @GetMapping
    @Operation(summary = "List Customers")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Returns a page of customers matching the search criteria, with pagination metadata"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "Invalid pagination or sorting parameters: page must be non-negative, perPage between 1 and 100, sort non-blank, and dir asc or desc"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Pagination<CustomerListResponse>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(name = "dir", defaultValue = "asc") String direction);

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update Customer")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Customer updated successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('customer:write') and @ownerAccess.isSelfOrAdmin(#id)")
    ResponseEntity<IdResponse> updateCustomer(@PathVariable("id") String id, @RequestBody UpdateCustomerRequest input);
}
