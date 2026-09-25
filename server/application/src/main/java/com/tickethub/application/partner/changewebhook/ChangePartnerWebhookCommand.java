package com.tickethub.application.partner.changewebhook;

public record ChangePartnerWebhookCommand(String id, String webhookUrl, String webhookSecret) {
    public static ChangePartnerWebhookCommand with(
            final String id, final String webhookUrl, final String webhookSecret) {
        return new ChangePartnerWebhookCommand(id, webhookUrl, webhookSecret);
    }
}
