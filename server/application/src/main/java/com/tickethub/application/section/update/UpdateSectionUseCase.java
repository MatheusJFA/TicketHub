package com.tickethub.application.section.update;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class UpdateSectionUseCase extends UseCase<UpdateSectionCommand, Either<Notification, UpdateSectionOutput>> {
}
