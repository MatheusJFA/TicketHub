package com.tickethub.domain.core.partner;

import java.util.Optional;

import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;

public interface PartnerGateway {
    Partner create(Partner partner);
    void deleteById(PartnerID id);
    Optional<Partner> findById(PartnerID id);
    Partner update(Partner partner);
    Pagination<Partner> findAll(SearchQuery query);
}