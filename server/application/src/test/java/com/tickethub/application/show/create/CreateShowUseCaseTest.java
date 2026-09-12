package com.tickethub.application.show.create;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.shared.Address;

public class CreateShowUseCaseTest extends UseCaseTest {
    private static final OffsetDateTime DATE = OffsetDateTime.parse("2027-01-15T20:00:00-03:00");
    private static final Address ADDRESS = Address.create("Rua Augusta", "100", null, "Centro", "São Paulo", "SP", "Brasil", "01305-000");

    @InjectMocks
    private DefaultCreateShowUseCase useCase;

    @Mock
    private ShowGateway showGateway;

    @Mock
    private PartnerGateway partnerGateway;

    @Override
    protected List<Object> getMocks() {
        return List.of(showGateway, partnerGateway);
    }

    @Test
    public void givenValidCommand_whenExecute_shouldPersistAndReturnId() {
        final var partner = Partner.create("Cinema Nova", "11222333000181", com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"));
        final var command = CreateShowCommand.with(partner.getId().getValue(), "Concert", "Description", DATE, ADDRESS, 10);
        when(partnerGateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        when(showGateway.create(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
        verify(partnerGateway, times(1)).findById(partner.getId());
        verify(showGateway, times(1)).create(argThat(saved ->
                saved.getId() != null
                        && saved.getId().getValue().equals(output.id())
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getPartnerId().equals(partner.getId())
                        && saved.getName().getValue().equals("Concert")
                        && saved.getDescription().getValue().equals("Description")
                        && saved.getAddress().equals(ADDRESS)
                        && saved.getDate().equals(DATE)
                        && saved.getTotalSpots() == 10
                        && saved.getTotalSpotsSold() == 0
                        && !saved.isPublished()
                        && saved.getSections().isEmpty()));
    }

    @Test
    public void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var partner = Partner.create("Cinema Nova", "11222333000181", com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"));
        final var command = CreateShowCommand.with(partner.getId().getValue(), "Concert", "Description", DATE, ADDRESS, 10);
        when(partnerGateway.findById(partner.getId())).thenReturn(Optional.of(partner));
        final var expectedMessage = "Gateway error";
        when(showGateway.create(any())).thenThrow(new IllegalStateException(expectedMessage));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals(expectedMessage, notification.firstError().message());
        verify(partnerGateway, times(1)).findById(partner.getId());
        verify(showGateway, times(1)).create(argThat(saved -> saved.getId() != null
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getPartnerId().equals(partner.getId())
                        && saved.getName().getValue().equals("Concert")
                        && saved.getDescription().getValue().equals("Description")
                        && saved.getAddress().equals(ADDRESS)
                        && saved.getDate().equals(DATE)
                        && saved.getTotalSpots() == 10
                        && saved.getTotalSpotsSold() == 0
                        && !saved.isPublished()
                        && saved.getSections().isEmpty()));
    }

    @Test
    public void givenMissingPartner_whenExecute_shouldNotPersist() {
        final var partner = Partner.create("Cinema Nova", "11222333000181", com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"));
        final var command = CreateShowCommand.with(partner.getId().getValue(), "Concert", "Description", DATE, ADDRESS, 10);
        when(partnerGateway.findById(partner.getId())).thenReturn(Optional.empty());

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Partner not found: " + partner.getId().getValue(), notification.firstError().message());
        verify(partnerGateway, times(1)).findById(partner.getId());
        verify(showGateway, never()).create(any());
    }

    @Test
    public void givenLookupFailure_whenExecute_shouldReturnNotification() {
        final var partner = Partner.create("Cinema Nova", "11222333000181", com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"));
        final var command = CreateShowCommand.with(partner.getId().getValue(), "Concert", "Description", DATE, ADDRESS, 10);
        when(partnerGateway.findById(partner.getId())).thenThrow(new IllegalStateException("Lookup failed"));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Lookup failed", notification.firstError().message());
        verify(partnerGateway, times(1)).findById(partner.getId());
        verify(showGateway, never()).create(any());
    }

    @Test
    public void givenInvalidCommand_whenExecute_shouldReturnValidationErrorsWithoutPersisting() {
        final var partner = Partner.create("Cinema Nova", "11222333000181", com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"));
        final var command = CreateShowCommand.with(partner.getId().getValue(), "Concert", "Description", DATE, null, -1);
        when(partnerGateway.findById(partner.getId())).thenReturn(Optional.of(partner));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(2, notification.getErrors().size());
        assertEquals("'address' should not be null", notification.firstError().message());
        verify(partnerGateway, times(1)).findById(partner.getId());
        verify(showGateway, never()).create(any());
    }
}
