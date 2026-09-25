package com.tickethub.application.partner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tickethub.application.partner.approve.DefaultApprovePartnerUseCase;
import com.tickethub.application.partner.reject.DefaultRejectPartnerUseCase;
import com.tickethub.domain.core.partner.*;
import com.tickethub.domain.shared.Address;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Partner approval use cases")
class PartnerApprovalUseCaseTest {
    private final PartnerGateway gateway = mock(PartnerGateway.class);
    private final Partner partner = Partner.create(
            "Cinema Nova",
            "11222333000181",
            Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
            "cinema@domain.com",
            "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS");
    private final String id = partner.getId().getValue();

    @Test
    @DisplayName("Approves pending partner")
    void approvesPendingPartner() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        when(gateway.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final var output = new DefaultApprovePartnerUseCase(gateway).execute(id).getRight();

        assertEquals(id, output.id());
        assertEquals("ACTIVE", output.status());
        assertEquals(PartnerStatus.ACTIVE, partner.getStatus());
    }

    @Test
    @DisplayName("Rejects pending partner")
    void rejectsPendingPartner() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        when(gateway.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final var output = new DefaultRejectPartnerUseCase(gateway).execute(id).getRight();

        assertEquals(id, output.id());
        assertEquals("REJECTED", output.status());
        assertEquals(PartnerStatus.REJECTED, partner.getStatus());
    }

    @Test
    @DisplayName("Approves missing partner as notification")
    void approvesMissingPartnerAsNotification() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.empty());

        assertEquals(
                "Partner not found: " + id,
                new DefaultApprovePartnerUseCase(gateway)
                        .execute(id)
                        .getLeft()
                        .firstError()
                        .message());
    }

    @Test
    @DisplayName("Approves non pending partner as notification")
    void approvesNonPendingPartnerAsNotification() {
        partner.approve();
        when(gateway.findById(partner.getId())).thenReturn(Optional.of(partner));

        assertEquals(
                "Only pending partners can be approved",
                new DefaultApprovePartnerUseCase(gateway)
                        .execute(id)
                        .getLeft()
                        .firstError()
                        .message());
    }

    @Test
    @DisplayName("Rejects missing partner as notification")
    void rejectsMissingPartnerAsNotification() {
        when(gateway.findById(partner.getId())).thenReturn(Optional.empty());

        assertEquals(
                "Partner not found: " + id,
                new DefaultRejectPartnerUseCase(gateway)
                        .execute(id)
                        .getLeft()
                        .firstError()
                        .message());
    }
}
