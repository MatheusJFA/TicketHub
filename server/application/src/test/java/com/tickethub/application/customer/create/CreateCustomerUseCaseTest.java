package com.tickethub.application.customer.create;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.auth.PasswordHasher;
import com.tickethub.domain.core.customer.CustomerGateway;

public class CreateCustomerUseCaseTest extends UseCaseTest {

    private static final String VALID_EMAIL = "john@domain.com";
    private static final String RAW_PASSWORD = "secret-123";
    private static final String PASSWORD_HASH = "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS";

    @InjectMocks
    private DefaultCreateCustomerUseCase useCase;

    @Mock
    private CustomerGateway customerGateway;

    @Mock
    private PasswordHasher passwordHasher;

    @Override
    protected List<Object> getMocks() {
        return List.of(customerGateway, passwordHasher);
    }

    @Test
    public void givenValidCommand_whenExecute_shouldPersistAndReturnId() {
        final var command = CreateCustomerCommand.with("12345678909", "John Doe", VALID_EMAIL, RAW_PASSWORD);
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(customerGateway.create(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
        verify(passwordHasher, times(1)).hash(RAW_PASSWORD);
        verify(customerGateway, times(1)).create(argThat(saved ->
                saved.getId() != null
                        && saved.getId().getValue().equals(output.id())
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getCpf().getValue().equals("12345678909")
                        && saved.getName().getValue().equals("John Doe")
                        && saved.getEmail().getValue().equals(VALID_EMAIL)
                        && saved.getPasswordHash().getValue().equals(PASSWORD_HASH)));
    }

    @Test
    public void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var command = CreateCustomerCommand.with("12345678909", "John Doe", VALID_EMAIL, RAW_PASSWORD);
        final var expectedMessage = "Gateway error";
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(customerGateway.create(any())).thenThrow(new IllegalStateException(expectedMessage));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals(expectedMessage, notification.firstError().message());
        verify(passwordHasher, times(1)).hash(RAW_PASSWORD);
        verify(customerGateway, times(1)).create(argThat(saved -> saved.getId() != null
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getCpf().getValue().equals("12345678909")
                        && saved.getName().getValue().equals("John Doe")
                        && saved.getEmail().getValue().equals(VALID_EMAIL)
                        && saved.getPasswordHash().getValue().equals(PASSWORD_HASH)));
    }

    @Test
    public void givenInvalidCommand_whenExecute_shouldReturnValidationErrorsWithoutPersisting() {
        final var command = CreateCustomerCommand.with("invalid", "John Doe", VALID_EMAIL, RAW_PASSWORD);
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Invalid CPF", notification.firstError().message());
        verify(passwordHasher, times(1)).hash(RAW_PASSWORD);
        verify(customerGateway, never()).create(any());
    }

    @Test
    public void givenInvalidEmail_whenExecute_shouldReturnValidationErrorsWithoutPersisting() {
        final var command = CreateCustomerCommand.with("12345678909", "John Doe", "not-an-email", RAW_PASSWORD);
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Invalid email", notification.firstError().message());
        verify(passwordHasher, times(1)).hash(RAW_PASSWORD);
        verify(customerGateway, never()).create(any());
    }
}
