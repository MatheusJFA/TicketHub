package com.tickethub.application.section.publish;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class PublishSectionUseCase extends UseCase<PublishSectionCommand, Either<Notification, PublishSectionOutput>> {
}
