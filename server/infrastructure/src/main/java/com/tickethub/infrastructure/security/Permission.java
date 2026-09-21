package com.tickethub.infrastructure.security;

public enum Permission {

    CUSTOMER_WRITE("customer:write"),
    CUSTOMER_DELETE("customer:delete"),

    PARTNER_WRITE("partner:write"),
    PARTNER_DELETE("partner:delete"),

    SHOW_CREATE("show:create"),
    SHOW_WRITE("show:write"),
    SHOW_PUBLISH("show:publish"),
    SHOW_DELETE("show:delete"),

    SECTION_WRITE("section:write"),
    SECTION_PUBLISH("section:publish"),
    SECTION_DELETE("section:delete"),

    SPOT_WRITE("spot:write"),
    SPOT_PUBLISH("spot:publish"),
    SPOT_DELETE("spot:delete"),

    TICKET_VALIDATE("ticket:validate"),

    ORDER_WRITE("order:write"),

    AUDIT_READ("audit:read");

    private final String authority;

    Permission(final String authority) {
        this.authority = authority;
    }

    public String authority() {
        return authority;
    }
}
