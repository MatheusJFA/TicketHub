package com.tickethub.application.customer.update;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tickethub.domain.core.customer.*;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Update customer use case")
class UpdateCustomerUseCaseTest {
    private final Customer entity = Customer.create(
            "52998224725",
            "Maria Silva",
            "maria@domain.com",
            "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS");
    private final CustomerGateway gateway = mock(CustomerGateway.class);
    private final DefaultUpdateCustomerUseCase useCase = new DefaultUpdateCustomerUseCase(gateway);
    private final String id = entity.getId().getValue();

    @Test
    @DisplayName("Updates name and persists")
    void updatesNameAndPersists() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gateway.update(entity)).thenReturn(entity);
        final var command = UpdateCustomerCommand.with(id, "Maria Souza");
        final var output = useCase.execute(command).getRight();
        assertEquals(id, output.id());
        assertEquals("Maria Souza", entity.getName().getValue());
        verify(gateway).update(entity);
    }

    @Test
    @DisplayName("Reports missing entity without persistence")
    void reportsMissingEntityWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.empty());
        final var command = UpdateCustomerCommand.with(id, "Maria Souza");
        final var result = useCase.execute(command);
        assertEquals("Customer not found: " + id, result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }
}
