package com.tickethub.infrastructure.operator.presenters;

import com.tickethub.application.operator.create.CreateOperatorCommand;
import com.tickethub.infrastructure.operator.models.CreateOperatorRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OperatorMapper {

    CreateOperatorCommand toCommand(CreateOperatorRequest request);
}
