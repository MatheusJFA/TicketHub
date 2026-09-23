package com.tickethub.application.zipcode.lookup;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.isNull;

import com.tickethub.application.Either;
import com.tickethub.domain.geography.ZipCodeAddress;
import com.tickethub.domain.geography.ZipCodeLookup;
import com.tickethub.domain.validation.Notification;

public class DefaultLookupZipCodeUseCase extends LookupZipCodeUseCase {
    private final ZipCodeLookup zipCodeLookup;

    public DefaultLookupZipCodeUseCase(final ZipCodeLookup zipCodeLookup) {
        this.zipCodeLookup = requireNonNull(zipCodeLookup, "'zipCodeLookup' should not be null");
    }

    @Override
    public Either<Notification, LookupZipCodeOutput> execute(final String zipCode) {
        try {
            final var normalized = ZipCodeAddress.normalize(zipCode);
            if (isNull(normalized)) {
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
