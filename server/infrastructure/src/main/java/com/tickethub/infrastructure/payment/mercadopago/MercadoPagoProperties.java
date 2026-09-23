package com.tickethub.infrastructure.payment.mercadopago;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tickethub.payment.mercadopago")
public class MercadoPagoProperties {

    private boolean enabled = true;
    private String baseUrl = "https://api.mercadopago.com";
    private String accessToken = "";
    private String payerEmail = "buyer@tickethub.local";
    private String webhookSecret = "";
    private Duration webhookTolerance = Duration.ofMinutes(5);
    private String notificationUrl = "";
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(5);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public void setPayerEmail(final String payerEmail) {
        this.payerEmail = payerEmail;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public void setWebhookSecret(final String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    public Duration getWebhookTolerance() {
        return webhookTolerance;
    }

    public void setWebhookTolerance(final Duration webhookTolerance) {
        this.webhookTolerance = webhookTolerance;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(final String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(final Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(final Duration readTimeout) {
        this.readTimeout = readTimeout;
    }
}
