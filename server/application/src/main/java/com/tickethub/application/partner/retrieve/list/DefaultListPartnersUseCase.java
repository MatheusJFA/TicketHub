package com.tickethub.application.partner.retrieve.list;
import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.core.partner.PartnerGateway;



public final class DefaultListPartnersUseCase extends ListPartnersUseCase {
    private final PartnerGateway partnerGateway;

    public DefaultListPartnersUseCase(final PartnerGateway partnerGateway) {
        this.partnerGateway = Objects.requireNonNull(partnerGateway);
    }

    @Override
    public Either<Notification, Pagination<ListPartnersOutput>> execute(final SearchQuery input) {
        Objects.requireNonNull(input);
        try {
            return Either.right(partnerGateway.findAll(input).map(ListPartnersOutput::from));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }
}
