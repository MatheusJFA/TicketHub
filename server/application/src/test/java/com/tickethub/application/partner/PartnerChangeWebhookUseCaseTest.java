package com.tickethub.application.partner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tickethub.application.partner.changewebhook.ChangePartnerWebhookCommand;
import com.tickethub.application.partner.changewebhook.DefaultChangePartnerWebhookUseCase;
import com.tickethub.domain.core.partner.*;
import com.tickethub.domain.shared.Address;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Partner change webhook use case")
class PartnerChangeWebhookUseCaseTest {
    private final PartnerGateway gateway = mock(PartnerGateway.class);
    private final Partner partner = Partner.create(
            "Cinema Nova",
            "11222333000181",
            Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
            "cinema@domain.com",
            "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS");
    private final String id = partner.getId().getValue();

    @Test
    @DisplayName("Changes webhook")
    void changesWebhook() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        when(gateway.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final var output = new DefaultChangePartnerWebhookUseCase(gateway)
                .execute(ChangePartnerWebhookCommand.with(id, "https://partner.domain.com/hook", "s3cr3t"))
                .getRight();

        assertEquals(id, output.id());
        assertEquals("https://partner.domain.com/hook", partner.getWebhookUrl());
        assertEquals("s3cr3t", partner.getWebhookSecret());
        verify(gateway).update(partner);
    }

    @Test
    @DisplayName("Changes missing partner as notification")
    void changesMissingPartnerAsNotification() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.empty());

        assertEquals(
                "Partner not found: " + id,
                new DefaultChangePartnerWebhookUseCase(gateway)
                        .execute(ChangePartnerWebhookCommand.with(id, "https://partner.domain.com/hook", "s3cr3t"))
                        .getLeft()
                        .firstError()
                        .message());
    }

    @Test
    @DisplayName("Changes webhook without secret as notification")
    void changesWebhookWithoutSecretAsNotification() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));

        assertEquals(
                "'webhookSecret' is required when 'webhookUrl' is set",
                new DefaultChangePartnerWebhookUseCase(gateway)
                        .execute(ChangePartnerWebhookCommand.with(id, "https://partner.domain.com/hook", null))
                        .getLeft()
                        .firstError()
                        .message());
        verify(gateway, never()).update(any());
    }
}
