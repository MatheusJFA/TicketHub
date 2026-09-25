package com.tickethub.infrastructure.api.controllers;

import com.tickethub.application.operator.create.CreateOperatorUseCase;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.api.OperatorAPI;
import com.tickethub.infrastructure.api.models.IdResponse;
import com.tickethub.infrastructure.operator.models.CreateOperatorRequest;
import com.tickethub.infrastructure.operator.presenters.OperatorMapper;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OperatorController implements OperatorAPI {
    private final CreateOperatorUseCase createOperator;
    private final OperatorMapper mapper;

    public OperatorController(final CreateOperatorUseCase createOperator, final OperatorMapper mapper) {
        this.createOperator = createOperator;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<IdResponse> createOperator(CreateOperatorRequest input) {
        final var output = HttpResults.require(createOperator.execute(mapper.toCommand(input)));
        return ResponseEntity.created(URI.create("/operators/" + output.id())).body(new IdResponse(output.id()));
    }
}
