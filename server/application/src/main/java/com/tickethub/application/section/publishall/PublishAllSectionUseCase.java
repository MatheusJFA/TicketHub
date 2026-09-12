package com.tickethub.application.section.publishall;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class PublishAllSectionUseCase extends UseCase<PublishAllSectionCommand, Either<Notification, PublishAllSectionOutput>> {
}
