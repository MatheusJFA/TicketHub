package com.tickethub.application.zipcode.lookup;

import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.geography.ZipCodeAddress;
import com.tickethub.domain.geography.ZipCodeLookup;
import com.tickethub.domain.validation.Notification;

public class DefaultLookupZipCodeUseCase extends LookupZipCodeUseCase {
    private final ZipCodeLookup zipCodeLookup;

    public DefaultLookupZipCodeUseCase(final ZipCodeLookup zipCodeLookup) {
        this.zipCodeLookup = Objects.requireNonNull(zipCodeLookup, "'zipCodeLookup' should not be null");
    }

    @Override
    public Either<Notification, LookupZipCodeOutput> execute(final String zipCode) {
        try {
            final var normalized = ZipCodeAddress.normalize(zipCode);
            if (normalized == null) {
                return Either.left(notFound(ZipCodeAddress.class.getSimpleName(), String.valueOf(zipCode)));
            }
            return findOrNotFound(
                    zipCodeLookup.lookup(zipCode).map(LookupZipCodeOutput::from),
                    ZipCodeAddress.class.getSimpleName(), normalized);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
