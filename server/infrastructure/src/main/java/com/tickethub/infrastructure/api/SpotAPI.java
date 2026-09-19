package com.tickethub.infrastructure.api;

import com.tickethub.infrastructure.api.models.*;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.application.spot.changelocation.*;
import com.tickethub.application.spot.create.*;
import com.tickethub.application.spot.delete.*;
import com.tickethub.application.spot.publish.*;
import com.tickethub.application.spot.retrieve.get.*;
import com.tickethub.application.spot.retrieve.list.*;
import com.tickethub.application.spot.unpublish.*;
import com.tickethub.infrastructure.spot.models.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;

@RequestMapping(value = "/spots", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Spots")
public interface SpotAPI {
    @PatchMapping(value = "/{id}/location", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Change Spot Location")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Spot location updated successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('spot:write') and @showAccess.canWriteSpot(#id)")
    ResponseEntity<?> changeSpotLocation(@PathVariable("id") String id, @RequestBody ChangeSpotLocationRequest input);

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Spot")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Spot created successfully; returns its identifier and the resource URL in the Location header"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('spot:write')")
    ResponseEntity<?> createSpot(@RequestBody CreateSpotRequest input);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Spot")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Spot deletion completed successfully; no response body"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('spot:delete') and @showAccess.canDeleteSpot(#id)")
    ResponseEntity<?> deleteById(@PathVariable("id") String id);

    @PostMapping(value = "/{id}/publish")
    @Operation(summary = "Publish Spot")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Spot published successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('spot:publish') and @showAccess.canPublishSpot(#id)")
    ResponseEntity<?> publishSpot(@PathVariable("id") String id);

    @GetMapping("/{id}")
    @Operation(summary = "Get Spot")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Spot found; returns the resource details"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    SpotResponse getById(@PathVariable("id") String id);

    @GetMapping
    @Operation(summary = "List Spots")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Returns a page of spots matching the search criteria, with pagination metadata"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "Invalid pagination or sorting parameters: page must be non-negative, perPage between 1 and 100, sort non-blank, and dir asc or desc"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    Pagination<SpotListResponse> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(name = "dir", defaultValue = "asc") String direction);

    @PostMapping(value = "/{id}/unpublish")
    @Operation(summary = "Unpublish Spot")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Spot unpublished successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('spot:publish') and @showAccess.canPublishSpot(#id)")
    ResponseEntity<?> unpublishSpot(@PathVariable("id") String id);
}
