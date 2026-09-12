package com.tickethub.application.customer.changename;

import java.util.List;
import java.util.Optional;
import java.util.Currency;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.tickethub.domain.shared.*;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.pagination.*;
import com.tickethub.domain.core.customer.*;

class ChangeCustomerNameUseCaseTest {
    private final Customer entity = Customer.create("52998224725", "Maria Silva");
    private final CustomerGateway gateway = mock(CustomerGateway.class);
    private final DefaultChangeCustomerNameUseCase useCase = new DefaultChangeCustomerNameUseCase(gateway);
    private final String id = entity.getId().getValue();
    private final String value = "Novo nome";

    @Test
    void changesOnlyRequestedFieldAndPersists() {
        final var createdAt = entity.getCreatedAt();
        final var updatedAt = entity.getUpdatedAt();
        final var originalcpf = entity.getCpf().getValue();
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gateway.update(entity)).thenReturn(entity);
        final var command = ChangeCustomerNameCommand.with(id, value);
        final var output = useCase.execute(command).getRight();
        assertEquals(id, output.id());
        assertEquals(value, entity.getName().getValue());
        assertEquals(createdAt, entity.getCreatedAt());
        assertFalse(entity.getUpdatedAt().isBefore(updatedAt));
        assertEquals(originalcpf, entity.getCpf().getValue());
        verify(gateway).update(entity);
    }

    @Test
    void rejectsInvalidValueWithoutMutationOrPersistence() {
        final var original = entity.getName();
        final var updatedAt = entity.getUpdatedAt();
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        final var command = ChangeCustomerNameCommand.with(id, null);
        final var notification = useCase.execute(command).getLeft();
        assertTrue(notification.hasError());
        assertEquals(original, entity.getName());
        assertEquals(updatedAt, entity.getUpdatedAt());
        verify(gateway, never()).update(any());
    }

    @Test
    void reportsMissingEntityWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.empty());
        final var command = ChangeCustomerNameCommand.with(id, value);
        final var result = useCase.execute(command);
        assertEquals("Customer not found: " + id, result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }

    @Test
    void reportsLookupFailureWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenThrow(new IllegalStateException("lookup failed"));
        final var command = ChangeCustomerNameCommand.with(id, value);
        final var result = useCase.execute(command);
        assertEquals("lookup failed", result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }

    @Test
    void reportsPersistenceFailure() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gateway.update(entity)).thenThrow(new IllegalStateException("save failed"));
        final var command = ChangeCustomerNameCommand.with(id, value);
        final var result = useCase.execute(command);
        assertEquals("save failed", result.getLeft().firstError().message());
    }
}
