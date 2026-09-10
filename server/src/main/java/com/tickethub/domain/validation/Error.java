package com.tickethub.domain.validation;

import com.tickethub.domain.ValueObject;

public record Error(String message) implements ValueObject {
}
