package com.tickethub.domain.authentication;

import com.tickethub.domain.exception.DomainException;

public class AuthenticationException extends DomainException {

    public AuthenticationException(final String message) {
        super(message);
    }
}
