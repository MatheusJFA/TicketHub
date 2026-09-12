package com.tickethub.application.section.delete;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class DeleteSectionUseCase extends UseCase<String, Either<Notification, DeleteSectionOutput>> {
}
