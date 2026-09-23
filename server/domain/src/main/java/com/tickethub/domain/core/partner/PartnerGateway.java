package com.tickethub.domain.core.partner;

import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.shared.Email;
import java.util.Optional;

public interface PartnerGateway {
    Partner create(Partner partner);

    void deleteById(PartnerID id);

    Optional<Partner> findById(PartnerID id);

    Optional<Partner> findByEmail(Email email);

    Partner update(Partner partner);

    Pagination<Partner> findAll(SearchQuery query);
}
