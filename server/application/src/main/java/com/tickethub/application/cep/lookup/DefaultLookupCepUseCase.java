package com.tickethub.application.cep.lookup;

import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.geo.CepAddress;
import com.tickethub.domain.geo.CepLookup;
import com.tickethub.domain.validation.Notification;

public class DefaultLookupCepUseCase extends LookupCepUseCase {
    private final CepLookup cepLookup;

    public DefaultLookupCepUseCase(final CepLookup cepLookup) {
        this.cepLookup = Objects.requireNonNull(cepLookup, "'cepLookup' should not be null");
    }

    @Override
    public Either<Notification, LookupCepOutput> execute(final String zipCode) {
        try {
            final var normalized = CepAddress.normalize(zipCode);
            if (normalized == null) {
                return Either.left(notFound(CepAddress.class.getSimpleName(), String.valueOf(zipCode)));
            }
            return findOrNotFound(
                    cepLookup.lookup(zipCode).map(LookupCepOutput::from),
                    CepAddress.class.getSimpleName(), normalized);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
