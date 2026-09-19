package com.tickethub.application.partner.create;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.auth.PasswordHasher;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.geo.CepAddress;
import com.tickethub.domain.geo.CepLookup;

public class CreatePartnerUseCaseTest extends UseCaseTest {

    private static final String VALID_EMAIL = "cinema@domain.com";
    private static final String RAW_PASSWORD = "secret-123";
    private static final String PASSWORD_HASH = "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS";

    @InjectMocks
    private DefaultCreatePartnerUseCase useCase;

    @Mock
    private PartnerGateway partnerGateway;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private CepLookup cepLookup;

    @Override
    protected List<Object> getMocks() {
        return List.of(partnerGateway, passwordHasher, cepLookup);
    }

    @Test
    public void givenValidCommand_whenExecute_shouldPersistAndReturnId() {
        final var command = CreatePartnerCommand.with("Cinema Nova", "11222333000181", com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"), VALID_EMAIL, RAW_PASSWORD);
        when(cepLookup.lookup("01001000")).thenReturn(Optional.empty());
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(partnerGateway.create(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
        verify(cepLookup, times(1)).lookup("01001000");
        verify(passwordHasher, times(1)).hash(RAW_PASSWORD);
        verify(partnerGateway, times(1)).create(argThat(saved ->
                saved.getId() != null
                        && saved.getId().getValue().equals(output.id())
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getName().getValue().equals("Cinema Nova")
                        && saved.getCnpj().getValue().equals("11222333000181")
                        && saved.getEmail().getValue().equals(VALID_EMAIL)
                        && saved.getPasswordHash().getValue().equals(PASSWORD_HASH)));
    }

    @Test
    public void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var command = CreatePartnerCommand.with("Cinema Nova", "11222333000181", com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"), VALID_EMAIL, RAW_PASSWORD);
        final var expectedMessage = "Gateway error";
        when(cepLookup.lookup("01001000")).thenReturn(Optional.empty());
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(partnerGateway.create(any())).thenThrow(new IllegalStateException(expectedMessage));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals(expectedMessage, notification.firstError().message());
        verify(cepLookup, times(1)).lookup("01001000");
        verify(passwordHasher, times(1)).hash(RAW_PASSWORD);
        verify(partnerGateway, times(1)).create(argThat(saved -> saved.getId() != null
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getName().getValue().equals("Cinema Nova")
                        && saved.getCnpj().getValue().equals("11222333000181")
                        && saved.getEmail().getValue().equals(VALID_EMAIL)
                        && saved.getPasswordHash().getValue().equals(PASSWORD_HASH)));
    }

    @Test
    public void givenInvalidCommand_whenExecute_shouldReturnValidationErrorsWithoutPersisting() {
        final var command = CreatePartnerCommand.with("Cinema Nova", "invalid", com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"), VALID_EMAIL, RAW_PASSWORD);
        when(cepLookup.lookup("01001000")).thenReturn(Optional.empty());
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Invalid CNPJ", notification.firstError().message());
        verify(cepLookup, times(1)).lookup("01001000");
        verify(passwordHasher, times(1)).hash(RAW_PASSWORD);
        verify(partnerGateway, never()).create(any());
    }

    @Test
    public void givenCepData_whenExecute_shouldOverwriteAddressExceptNumber() {
        final var command = CreatePartnerCommand.with("Cinema Nova", "11222333000181", com.tickethub.domain.shared.Address.create("Rua X", "42", "Sala 9", "Bairro X", "Cidade X", "XX", "Brasil", "01305-000"), VALID_EMAIL, RAW_PASSWORD);
        when(cepLookup.lookup("01305-000")).thenReturn(Optional.of(
                new CepAddress("01305000", "Avenida Paulista", "Bela Vista", "São Paulo", "SP", "Brasil")));
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(partnerGateway.create(any())).thenAnswer(returnsFirstArg());

        useCase.execute(command).getRight();

        verify(cepLookup, times(1)).lookup("01305-000");
        verify(partnerGateway, times(1)).create(argThat(saved ->
                saved.getAddress().getStreet().equals("Avenida Paulista")
                        && saved.getAddress().getNeighborhood().equals("Bela Vista")
                        && saved.getAddress().getCity().equals("São Paulo")
                        && saved.getAddress().getState().equals("SP")
                        && saved.getAddress().getNumber().equals("42")
                        && saved.getAddress().getComplement().equals("Sala 9")));
    }
}
