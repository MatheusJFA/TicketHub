package com.tickethub.application.customer.retrieve.list;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.core.customer.CustomerGateway;



public final class DefaultListCustomersUseCase extends ListCustomersUseCase {
    private final CustomerGateway customerGateway;

    public DefaultListCustomersUseCase(final CustomerGateway customerGateway) {
        this.customerGateway = Objects.requireNonNull(customerGateway);
    }

    @Override
    public Either<Notification, Pagination<ListCustomersOutput>> execute(final SearchQuery input) {
        Objects.requireNonNull(input);
        try {
            final var page = customerGateway.findAll(input);
            final var output = page.map(ListCustomersOutput::from);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
