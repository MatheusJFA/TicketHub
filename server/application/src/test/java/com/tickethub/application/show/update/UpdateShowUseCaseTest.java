package com.tickethub.application.show.update;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.tickethub.domain.shared.*;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.show.*;

class UpdateShowUseCaseTest {
    private final Address address = Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil",
            "01001000");
    private final Show entity = Show.create("Show original", "Descricao original",
            OffsetDateTime.parse("2030-01-01T20:00:00Z"), address, 10, PartnerID.generate());
    private final ShowGateway gateway = mock(ShowGateway.class);
    private final DefaultUpdateShowUseCase useCase = new DefaultUpdateShowUseCase(gateway);
    private final String id = entity.getId().getValue();

    @Test
    void updatesAllFieldsAndPersists() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gateway.update(entity)).thenReturn(entity);
        final var date = OffsetDateTime.parse("2030-06-01T21:00:00Z");
        final var command = UpdateShowCommand.with(id, "Show novo", "Descricao nova", date);
        final var output = useCase.execute(command).getRight();
        assertEquals(id, output.id());
        assertEquals("Show novo", entity.getName().getValue());
        assertEquals("Descricao nova", entity.getDescription().getValue());
        assertEquals(date, entity.getDate());
        verify(gateway).update(entity);
    }

    @Test
    void reportsMissingEntityWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.empty());
        final var command = UpdateShowCommand.with(id, "Show novo", "Descricao nova",
                OffsetDateTime.parse("2030-06-01T21:00:00Z"));
        final var result = useCase.execute(command);
        assertEquals("Show not found: " + id, result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }
}
