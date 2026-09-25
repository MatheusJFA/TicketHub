package com.tickethub.application.partner.changewebhook;

import com.tickethub.domain.core.partner.Partner;

public record ChangePartnerWebhookOutput(String id) {
    public static ChangePartnerWebhookOutput from(final String id) {
        return new ChangePartnerWebhookOutput(id);
    }

    public static ChangePartnerWebhookOutput from(final Partner entity) {
        return from(entity.getId().getValue());
    }
}
