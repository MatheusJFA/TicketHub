package com.tickethub.application.operator.create;

import com.tickethub.domain.core.operator.Operator;

public record CreateOperatorOutput(String id) {
    public static CreateOperatorOutput from(final String id) {
        return new CreateOperatorOutput(id);
    }

    public static CreateOperatorOutput from(final Operator entity) {
        return from(entity.getId().getValue());
    }
}
