package com.tickethub.application.partner.update;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.tickethub.domain.shared.*;
import com.tickethub.domain.core.partner.*;

class UpdatePartnerUseCaseTest {
    private final Address address = Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil",
            "01001000");
    private final Address newAddress = Address.create("Rua B", "20", "Sala 1", "Centro", "Sao Paulo", "SP", "Brasil",
            "01001001");
    private final Partner entity = Partner.create("Cinema", "11222333000181", address);
    private final PartnerGateway gateway = mock(PartnerGateway.class);
    private final DefaultUpdatePartnerUseCase useCase = new DefaultUpdatePartnerUseCase(gateway);
    private final String id = entity.getId().getValue();

    @Test
    void updatesAllFieldsAndPersists() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gateway.update(entity)).thenReturn(entity);
        final var command = UpdatePartnerCommand.with(id, "Cinema Novo", newAddress);
        final var output = useCase.execute(command).getRight();
        assertEquals(id, output.id());
        assertEquals("Cinema Novo", entity.getName().getValue());
        assertEquals(newAddress, entity.getAddress());
        verify(gateway).update(entity);
    }

    @Test
    void reportsMissingEntityWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.empty());
        final var command = UpdatePartnerCommand.with(id, "Cinema Novo", newAddress);
        final var result = useCase.execute(command);
        assertEquals("Partner not found: " + id, result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }
}
