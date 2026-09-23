package com.tickethub.application.customer.retrieve.list;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.validation.Notification;

public class DefaultListCustomersUseCase extends ListCustomersUseCase {
    private final CustomerGateway customerGateway;

    public DefaultListCustomersUseCase(final CustomerGateway customerGateway) {
        this.customerGateway = requireNonNull(customerGateway);
    }

    @Override
    public Either<Notification, Pagination<ListCustomersOutput>> execute(final SearchQuery input) {
        try {
            final Pagination<Customer> page = customerGateway.findAll(input);
            final Pagination<ListCustomersOutput> output = page.map(ListCustomersOutput::from);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
