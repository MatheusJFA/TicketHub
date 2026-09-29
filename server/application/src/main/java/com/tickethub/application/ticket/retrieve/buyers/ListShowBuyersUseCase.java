package com.tickethub.application.ticket.retrieve.buyers;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Notification;
import java.util.List;

public abstract class ListShowBuyersUseCase
        extends UseCase<ListShowBuyersCommand, Either<Notification, List<ListShowBuyersOutput>>> {}
