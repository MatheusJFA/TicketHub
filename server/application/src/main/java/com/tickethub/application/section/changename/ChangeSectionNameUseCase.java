package com.tickethub.application.section.changename;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class ChangeSectionNameUseCase extends UseCase<ChangeSectionNameCommand, Either<Notification, ChangeSectionNameOutput>> {
}
