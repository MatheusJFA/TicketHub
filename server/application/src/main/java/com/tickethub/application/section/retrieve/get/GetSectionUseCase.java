package com.tickethub.application.section.retrieve.get;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;

public abstract class GetSectionUseCase extends UseCase<String, Either<Notification, GetSectionOutput>> {}
