package com.tickethub.application.zipcode.lookup;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

/**
 * Dedicated ZIP code lookup, used by clients to autofill address forms before
 * registration. Create/update flows no longer call the provider: they
 * persist the address exactly as submitted.
 */
public abstract class LookupZipCodeUseCase extends UseCase<String, Either<Notification, LookupZipCodeOutput>> {
}
