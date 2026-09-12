package com.tickethub.application.customer.create;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.customer.CustomerGateway;

public class CreateCustomerUseCaseTest extends UseCaseTest {

    @InjectMocks
    private DefaultCreateCustomerUseCase useCase;

    @Mock
    private CustomerGateway customerGateway;

    @Override
    protected List<Object> getMocks() {
        return List.of(customerGateway);
    }

    @Test
    public void givenValidCommand_whenExecute_shouldPersistAndReturnId() {
        final var command = CreateCustomerCommand.with("12345678909", "John Doe");
        when(customerGateway.create(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
        verify(customerGateway, times(1)).create(argThat(saved ->
                saved.getId() != null
                        && saved.getId().getValue().equals(output.id())
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getCpf().getValue().equals("12345678909")
                        && saved.getName().getValue().equals("John Doe")));
    }

    @Test
    public void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var command = CreateCustomerCommand.with("12345678909", "John Doe");
        final var expectedMessage = "Gateway error";
        when(customerGateway.create(any())).thenThrow(new IllegalStateException(expectedMessage));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals(expectedMessage, notification.firstError().message());
        verify(customerGateway, times(1)).create(argThat(saved -> saved.getId() != null
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getCpf().getValue().equals("12345678909")
                        && saved.getName().getValue().equals("John Doe")));
    }

    @Test
    public void givenInvalidCommand_whenExecute_shouldReturnValidationErrorsWithoutPersisting() {
        final var command = CreateCustomerCommand.with("invalid", "John Doe");

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Invalid CPF", notification.firstError().message());
        verify(customerGateway, never()).create(any());
    }
}
