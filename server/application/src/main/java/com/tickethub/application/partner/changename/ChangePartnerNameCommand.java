package com.tickethub.application.partner.changename;

public record ChangePartnerNameCommand(String id, String name) {
    public static ChangePartnerNameCommand with(final String id, final String name) {
        return new ChangePartnerNameCommand(id, name);
    }
}
