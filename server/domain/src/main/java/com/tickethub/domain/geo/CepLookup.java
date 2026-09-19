package com.tickethub.domain.geo;

import java.util.Optional;

public interface CepLookup {

    /**
     * Looks up address data for a zip code.
     *
     * <p>Implementations must never throw: an unknown zip code, a disabled
     * provider or any upstream failure results in {@link Optional#empty()},
     * letting callers fall back to user-supplied data (fail-open).
     */
    Optional<CepAddress> lookup(String zipCode);
}
