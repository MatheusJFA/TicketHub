package com.tickethub.application.section.changedescription;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class ChangeSectionDescriptionUseCase extends UseCase<ChangeSectionDescriptionCommand, Either<Notification, ChangeSectionDescriptionOutput>> {
}
