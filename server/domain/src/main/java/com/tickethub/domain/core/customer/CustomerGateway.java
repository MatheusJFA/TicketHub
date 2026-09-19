package com.tickethub.domain.core.customer;

import java.util.Optional;

import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.shared.Email;

public interface CustomerGateway {
    Customer create(Customer customer);
    void deleteById(CustomerID id);
    Optional<Customer> findById(CustomerID id);
    Optional<Customer> findByEmail(Email email);
    Customer update(Customer customer);
    Pagination<Customer> findAll(SearchQuery query);
}