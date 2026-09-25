package com.tickethub.infrastructure.order.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Body for opening an order: the buying customer and the spots to reserve.
 * Every spot must be published and free; the first unavailable spot aborts
 * the whole order and releases the spots already reserved. An optional
 * coupon code discounts eligible items (422 when unknown, expired,
 * exhausted or not applicable).
 */
public record CreateOrderRequest(
        @NotBlank String customerId, @NotEmpty List<@NotBlank String> spotIds, String couponCode) {}
