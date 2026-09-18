package com.tickethub.application.section.generatespots;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class GenerateSectionSpotsUseCase
        extends UseCase<GenerateSectionSpotsCommand, Either<Notification, GenerateSectionSpotsOutput>> {
}
