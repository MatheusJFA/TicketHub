package com.tickethub.infrastructure.api;

import com.tickethub.infrastructure.api.models.*;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.application.section.changedescription.*;
import com.tickethub.application.section.changename.*;
import com.tickethub.application.section.changeprice.*;
import com.tickethub.application.section.create.*;
import com.tickethub.application.section.delete.*;
import com.tickethub.application.section.publish.*;
import com.tickethub.application.section.publishall.*;
import com.tickethub.application.section.retrieve.get.*;
import com.tickethub.application.section.retrieve.list.*;
import com.tickethub.application.section.unpublish.*;
import com.tickethub.application.section.unpublishall.*;
import com.tickethub.infrastructure.section.models.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;

@RequestMapping(value = "/sections", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Sections")
public interface SectionAPI {
    @PatchMapping(value = "/{id}/description", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Change Section Description")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Section description updated successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('section:write')")
    ResponseEntity<?> changeSectionDescription(@PathVariable("id") String id, @RequestBody ChangeSectionDescriptionRequest input);

    @PatchMapping(value = "/{id}/name", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Change Section Name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Section name updated successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('section:write')")
    ResponseEntity<?> changeSectionName(@PathVariable("id") String id, @RequestBody ChangeSectionNameRequest input);

    @PatchMapping(value = "/{id}/price", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Change Section Price")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Section price updated successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('section:write')")
    ResponseEntity<?> changeSectionPrice(@PathVariable("id") String id, @RequestBody ChangeSectionPriceRequest input);

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Section")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Section created successfully; returns its identifier and the resource URL in the Location header"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('section:write')")
    ResponseEntity<?> createSection(@RequestBody CreateSectionRequest input);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Section")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Section deletion completed successfully; no response body"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('section:delete')")
    ResponseEntity<?> deleteById(@PathVariable("id") String id);

    @PostMapping(value = "/{id}/publish")
    @Operation(summary = "Publish Section")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Section published successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('section:publish')")
    ResponseEntity<?> publishSection(@PathVariable("id") String id);

    @PostMapping(value = "/{id}/publish-all")
    @Operation(summary = "Publish All Section")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Section and its nested resources published successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('section:publish')")
    ResponseEntity<?> publishAllSection(@PathVariable("id") String id);

    @GetMapping("/{id}")
    @Operation(summary = "Get Section")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Section found; returns the resource details"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    SectionResponse getById(@PathVariable("id") String id);

    @GetMapping
    @Operation(summary = "List Sections")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Returns a page of sections matching the search criteria, with pagination metadata"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "Invalid pagination or sorting parameters: page must be non-negative, perPage between 1 and 100, sort non-blank, and dir asc or desc"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    Pagination<SectionListResponse> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(name = "dir", defaultValue = "asc") String direction);

    @PostMapping(value = "/{id}/unpublish")
    @Operation(summary = "Unpublish Section")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Section unpublished successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('section:publish')")
    ResponseEntity<?> unpublishSection(@PathVariable("id") String id);

    @PostMapping(value = "/{id}/unpublish-all")
    @Operation(summary = "Unpublish All Section")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Section and its nested resources unpublished successfully; returns the resource identifier"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The requested resource or a referenced resource was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('section:publish')")
    ResponseEntity<?> unpublishAllSection(@PathVariable("id") String id);
}
