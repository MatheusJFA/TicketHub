package com.tickethub.domain.exception;

/**
 * A requested aggregate or entity does not exist. Carries the resource name
 * and identifier so logs and responses stay easy to identify.
 */
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(final String resource, final String id) {
        super(resource + " not found: " + id);
    }
}
