package com.tickethub.infrastructure.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tickethub.infrastructure.order.models.CreateOrderRequest;
import com.tickethub.infrastructure.order.models.OrderResponse;
import com.tickethub.infrastructure.order.models.PayOrderResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RequestMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Orders")
public interface OrderAPI {
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Order",
            description = "Opens a PENDING order and atomically reserves each spot for 15 minutes; returns the order with its expiry. "
                    + "Send Idempotency-Key to make retries safe: the same key returns the original order instead of reserving twice")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order opened (or replayed for a known Idempotency-Key); returns the order and its URL in the Location header"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The customer or one of the spots was not found for the supplied identifiers"),
        @ApiResponse(responseCode = "422", description = "One of the spots is unpublished, already reserved or used, or has no price"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('order:write')")
    ResponseEntity<OrderResponse> createOrder(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest input);

    @PostMapping(value = "/{id}/pay", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Pay Order",
            description = "Creates a provider charge for a PENDING order; retrying an already charged order returns the current charge")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Charge created (or already existing) for the order"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The order was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The reservation expired before payment"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('order:write')")
    ResponseEntity<PayOrderResponse> payOrder(@PathVariable("id") String id);

    @PostMapping(value = "/{id}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancel Order",
            description = "Cancels an open order and releases its spots back on sale; paid orders cannot be cancelled")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order cancelled and spots released"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The order was not found for the supplied identifier"),
        @ApiResponse(responseCode = "422", description = "The order is already paid and cannot be cancelled"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('order:write')")
    ResponseEntity<OrderResponse> cancelOrder(@PathVariable("id") String id);

    @GetMapping(value = "/{id}")
    @Operation(summary = "Get Order",
            description = "Returns an order with its status, charge and reserved spots; use it to poll payment confirmation")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order details"),
        @ApiResponse(responseCode = "400", description = "The request body is missing or contains malformed JSON, or a request parameter has an incompatible type"),
        @ApiResponse(responseCode = "404", description = "The order was not found for the supplied identifier"),
        @ApiResponse(responseCode = "500", description = "An unexpected failure prevented the server from completing the operation"),
        @ApiResponse(responseCode = "503", description = "The operation is unavailable because its required service dependencies are not configured")
    })
    @PreAuthorize("hasAuthority('order:write')")
    ResponseEntity<OrderResponse> getOrder(@PathVariable("id") String id);
}
