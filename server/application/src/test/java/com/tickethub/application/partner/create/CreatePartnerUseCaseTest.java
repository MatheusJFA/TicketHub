package com.tickethub.application.partner.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.authentication.PasswordHasher;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.shared.Address;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("Create partner use case")
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

    @Override
    protected List<Object> getMocks() {
        return List.of(partnerGateway, passwordHasher);
    }

    @Test
    @DisplayName("Given valid command, when execute, should persist and return id")
    public void givenValidCommand_whenExecute_shouldPersistAndReturnId() {
        final var command = CreatePartnerCommand.with(
                "Cinema Nova",
                "11222333000181",
                Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                VALID_EMAIL,
                RAW_PASSWORD);
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(partnerGateway.create(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
        verify(passwordHasher, times(1)).hash(RAW_PASSWORD);
        verify(partnerGateway, times(1))
                .create(argThat(saved -> saved.getId() != null
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
    @DisplayName("Given gateway failure, when execute, should return notification")
    public void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var command = CreatePartnerCommand.with(
                "Cinema Nova",
                "11222333000181",
                Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                VALID_EMAIL,
                RAW_PASSWORD);
        final var expectedMessage = "Gateway error";
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(partnerGateway.create(any())).thenThrow(new IllegalStateException(expectedMessage));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals(expectedMessage, notification.firstError().message());
        verify(passwordHasher, times(1)).hash(RAW_PASSWORD);
        verify(partnerGateway, times(1))
                .create(argThat(saved -> saved.getId() != null
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getName().getValue().equals("Cinema Nova")
                        && saved.getCnpj().getValue().equals("11222333000181")
                        && saved.getEmail().getValue().equals(VALID_EMAIL)
                        && saved.getPasswordHash().getValue().equals(PASSWORD_HASH)));
    }

    @Test
    @DisplayName("Given invalid command, when execute, should return validation errors without persisting")
    public void givenInvalidCommand_whenExecute_shouldReturnValidationErrorsWithoutPersisting() {
        final var command = CreatePartnerCommand.with(
                "Cinema Nova",
                "invalid",
                Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                VALID_EMAIL,
                RAW_PASSWORD);
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Invalid CNPJ", notification.firstError().message());
        verify(passwordHasher, times(1)).hash(RAW_PASSWORD);
        verify(partnerGateway, never()).create(any());
    }

    @Test
    @DisplayName("Given submitted address, when execute, should persist it as is without lookup")
    public void givenSubmittedAddress_whenExecute_shouldPersistItAsIs() {
        final var command = CreatePartnerCommand.with(
                "Cinema Nova",
                "11222333000181",
                Address.create("Rua X", "42", "Sala 9", "Bairro X", "Cidade X", "XX", "Brasil", "01305-000"),
                VALID_EMAIL,
                RAW_PASSWORD);
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(partnerGateway.create(any())).thenAnswer(returnsFirstArg());

        useCase.execute(command).getRight();

        verify(partnerGateway, times(1))
                .create(argThat(saved -> saved.getAddress().getStreet().equals("Rua X")
                        && saved.getAddress().getNeighborhood().equals("Bairro X")
                        && saved.getAddress().getCity().equals("Cidade X")
                        && saved.getAddress().getState().equals("XX")
                        && saved.getAddress().getNumber().equals("42")
                        && saved.getAddress().getComplement().equals("Sala 9")
                        && saved.getAddress().getZipCode().equals("01305-000")));
    }
}
