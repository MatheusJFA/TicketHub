package com.tickethub.infrastructure.api;

import com.tickethub.infrastructure.api.models.*;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.application.show.addsection.*;
import com.tickethub.application.show.changedescription.*;
import com.tickethub.application.show.changename.*;
import com.tickethub.application.show.create.*;
import com.tickethub.application.show.delete.*;
import com.tickethub.application.show.publish.*;
import com.tickethub.application.show.publishall.*;
import com.tickethub.application.show.reschedule.*;
import com.tickethub.application.show.retrieve.get.*;
import com.tickethub.application.show.retrieve.list.*;
import com.tickethub.application.show.unpublish.*;
import com.tickethub.application.show.unpublishall.*;
import com.tickethub.infrastructure.show.models.*;
import com.tickethub.infrastructure.section.models.SectionListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;

@RequestMapping(value = "/shows", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Shows")
public interface ShowAPI {
    @PostMapping(value = "/{id}/sections", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add Section To Show")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Section added to the show successfully; returns the show identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("@showAccess.canWrite(#id)")
    ResponseEntity<IdResponse> addSectionToShow(@PathVariable("id") String id, @RequestBody AddSectionToShowRequest input);

    @PatchMapping(value = "/{id}/description", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Change Show Description")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Show description updated successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("@showAccess.canWrite(#id)")
    ResponseEntity<IdResponse> changeShowDescription(@PathVariable("id") String id, @RequestBody ChangeShowDescriptionRequest input);

    @PatchMapping(value = "/{id}/name", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Change Show Name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Show name updated successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("@showAccess.canWrite(#id)")
    ResponseEntity<IdResponse> changeShowName(@PathVariable("id") String id, @RequestBody ChangeShowNameRequest input);

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Show")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Show created successfully; returns its identifier and the resource URL in the Location header"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("@showAccess.canCreate(#input.partnerId())")
    ResponseEntity<IdResponse> createShow(@RequestBody CreateShowRequest input);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Show")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Show deletion completed successfully; no response body"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("@showAccess.canDelete(#id)")
    ResponseEntity<Void> deleteById(@PathVariable("id") String id);

    @PostMapping(value = "/{id}/publish")
    @Operation(summary = "Publish Show")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Show published successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("@showAccess.canPublish(#id)")
    ResponseEntity<IdResponse> publishShow(@PathVariable("id") String id);

    @PostMapping(value = "/{id}/publish-all")
    @Operation(summary = "Publish All Show")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Show and its nested resources published successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("@showAccess.canPublish(#id)")
    ResponseEntity<IdResponse> publishAllShow(@PathVariable("id") String id);

    @PatchMapping(value = "/{id}/date", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Reschedule Show")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Show date updated successfully; returns the show identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("@showAccess.canWrite(#id)")
    ResponseEntity<IdResponse> rescheduleShow(@PathVariable("id") String id, @RequestBody RescheduleShowRequest input);

    @GetMapping("/{id}")
    @Operation(summary = "Get Show")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Show found; returns the resource details"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    ShowResponse getById(@PathVariable("id") String id);

    @GetMapping("/{id}/sections")
    @Operation(summary = "List Show Sections",
            description = "Returns a page of sections belonging to the show, for the seat map")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Returns a page of sections belonging to the show"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "422", description = "Invalid pagination or sorting parameters: page must be non-negative, perPage between 1 and 100, sort non-blank, and dir asc or desc"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    Pagination<SectionListResponse> listSections(
            @PathVariable("id") String id,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(name = "dir", defaultValue = "asc") String direction);

    @GetMapping
    @Operation(summary = "List Shows")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Returns a page of shows matching the search criteria, with pagination metadata"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "Invalid pagination or sorting parameters: page must be non-negative, perPage between 1 and 100, sort non-blank, and dir asc or desc"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    Pagination<ShowListResponse> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(name = "dir", defaultValue = "asc") String direction);

    @PostMapping(value = "/{id}/unpublish")
    @Operation(summary = "Unpublish Show")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Show unpublished successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("@showAccess.canPublish(#id)")
    ResponseEntity<IdResponse> unpublishShow(@PathVariable("id") String id);

    @PostMapping(value = "/{id}/unpublish-all")
    @Operation(summary = "Unpublish All Show")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Show and its nested resources unpublished successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("@showAccess.canPublish(#id)")
    ResponseEntity<IdResponse> unpublishAllShow(@PathVariable("id") String id);

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update Show")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Show updated successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("@showAccess.canWrite(#id)")
    ResponseEntity<IdResponse> updateShow(@PathVariable("id") String id, @RequestBody UpdateShowRequest input);
}
