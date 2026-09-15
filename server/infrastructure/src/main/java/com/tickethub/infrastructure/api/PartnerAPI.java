package com.tickethub.infrastructure.api;

import com.tickethub.infrastructure.api.models.*;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.application.partner.changeaddress.*;
import com.tickethub.application.partner.changename.*;
import com.tickethub.application.partner.create.*;
import com.tickethub.application.partner.delete.*;
import com.tickethub.application.partner.retrieve.get.*;
import com.tickethub.application.partner.retrieve.list.*;
import com.tickethub.infrastructure.partner.models.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/partners", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Partners")
public interface PartnerAPI {
    @PatchMapping(value = "/{id}/address", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Change Partner Address")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Partner address updated successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    ResponseEntity<?> changePartnerAddress(@PathVariable("id") String id, @RequestBody ChangePartnerAddressRequest input);

    @PatchMapping(value = "/{id}/name", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Change Partner Name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Partner name updated successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    ResponseEntity<?> changePartnerName(@PathVariable("id") String id, @RequestBody ChangePartnerNameRequest input);

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Partner")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Partner created successfully; returns its identifier and the resource URL in the Location header"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    ResponseEntity<?> createPartner(@RequestBody CreatePartnerRequest input);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Partner")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Partner deletion completed successfully; no response body"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    ResponseEntity<?> deleteById(@PathVariable("id") String id);

    @GetMapping("/{id}")
    @Operation(summary = "Get Partner")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Partner found; returns the resource details"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    ResponseEntity<?> getById(@PathVariable("id") String id);

    @GetMapping
    @Operation(summary = "List Partners")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Returns a page of partners matching the search criteria, with pagination metadata"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "Invalid pagination or sorting parameters: page must be non-negative, perPage between 1 and 100, sort non-blank, and dir asc or desc"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    ResponseEntity<?> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(name = "dir", defaultValue = "asc") String direction);
}
