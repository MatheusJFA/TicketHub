package com.tickethub.application.section.changeprice;

import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.application.UseCase;

public abstract class ChangeSectionPriceUseCase extends UseCase<ChangeSectionPriceCommand, Either<Notification, ChangeSectionPriceOutput>> {
}
