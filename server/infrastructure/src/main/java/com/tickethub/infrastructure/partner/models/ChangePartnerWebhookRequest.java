package com.tickethub.infrastructure.partner.models;

public record ChangePartnerWebhookRequest(String webhookUrl, String webhookSecret) {}
