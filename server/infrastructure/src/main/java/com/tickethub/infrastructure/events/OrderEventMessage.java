package com.tickethub.infrastructure.events;

public record OrderEventMessage(String type, String orderId, String occurredOn) {

    public static final String PAID_TYPE = "OrderPaid";
    public static final String REFUNDED_TYPE = "OrderRefunded";
}
