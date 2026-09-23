package com.tickethub.application.partner.retrieve.list;

import static java.util.Objects.requireNonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.validation.Notification;

public class DefaultListPartnersUseCase extends ListPartnersUseCase {
    private final PartnerGateway partnerGateway;

    public DefaultListPartnersUseCase(final PartnerGateway partnerGateway) {
        this.partnerGateway = requireNonNull(partnerGateway);
    }

    @Override
    public Either<Notification, Pagination<ListPartnersOutput>> execute(final SearchQuery input) {
        try {
            final Pagination<Partner> page = partnerGateway.findAll(input);
            final Pagination<ListPartnersOutput> output = page.map(ListPartnersOutput::from);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
