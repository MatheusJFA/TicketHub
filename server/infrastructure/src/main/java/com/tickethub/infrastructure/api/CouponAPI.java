package com.tickethub.infrastructure.api;

import com.tickethub.infrastructure.api.models.IdResponse;
import com.tickethub.infrastructure.coupon.models.CreateCouponRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/coupons", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Coupons")
public interface CouponAPI {

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Coupon (master only)")
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description =
                        "Coupon created successfully; returns its identifier and the resource URL in the Location header"),
        @ApiResponse(
                responseCode = "400",
                description =
                        "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid credentials"),
        @ApiResponse(responseCode = "403", description = "The authenticated actor lacks the ADMIN role"),
        @ApiResponse(
                responseCode = "422",
                description =
                        "The request was parsed, but its values or the current resource state violate domain validation rules"),
        @ApiResponse(
                responseCode = "500",
                description = "An unexpected failure prevented the server from completing the operation")
    })
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<IdResponse> createCoupon(@RequestBody CreateCouponRequest input);
}
