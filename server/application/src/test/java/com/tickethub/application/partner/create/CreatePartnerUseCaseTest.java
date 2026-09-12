package com.tickethub.application.partner.create;

import java.util.List;

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
import com.tickethub.domain.core.partner.PartnerGateway;

public class CreatePartnerUseCaseTest extends UseCaseTest {

    @InjectMocks
    private DefaultCreatePartnerUseCase useCase;

    @Mock
    private PartnerGateway partnerGateway;

    @Override
    protected List<Object> getMocks() {
        return List.of(partnerGateway);
    }

    @Test
    public void givenValidCommand_whenExecute_shouldPersistAndReturnId() {
        final var command = CreatePartnerCommand.with("Cinema Nova", "11222333000181", com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"));
        when(partnerGateway.create(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
        verify(partnerGateway, times(1)).create(argThat(saved ->
                saved.getId() != null
                        && saved.getId().getValue().equals(output.id())
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getName().getValue().equals("Cinema Nova")
                        && saved.getCnpj().getValue().equals("11222333000181")));
    }

    @Test
    public void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var command = CreatePartnerCommand.with("Cinema Nova", "11222333000181", com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"));
        final var expectedMessage = "Gateway error";
        when(partnerGateway.create(any())).thenThrow(new IllegalStateException(expectedMessage));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals(expectedMessage, notification.firstError().message());
        verify(partnerGateway, times(1)).create(argThat(saved -> saved.getId() != null
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getName().getValue().equals("Cinema Nova")
                        && saved.getCnpj().getValue().equals("11222333000181")));
    }

    @Test
    public void givenInvalidCommand_whenExecute_shouldReturnValidationErrorsWithoutPersisting() {
        final var command = CreatePartnerCommand.with("Cinema Nova", "invalid", com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Invalid CNPJ", notification.firstError().message());
        verify(partnerGateway, never()).create(any());
    }
}
