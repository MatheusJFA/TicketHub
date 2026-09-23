package com.tickethub.infrastructure.customer;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.contains;

import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.shared.Email;
import com.tickethub.infrastructure.customer.persistence.CustomerDocument;
import com.tickethub.infrastructure.customer.persistence.CustomerRepository;
import com.tickethub.infrastructure.shared.persistence.MongoGatewaySupport;
import java.util.Optional;
import java.util.Set;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomerMongoGateway implements CustomerGateway {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "cpf", "createdAt", "updatedAt");

    private final MongoTemplate mongoTemplate;
    private final CustomerRepository repository;

    public CustomerMongoGateway(final MongoTemplate mongoTemplate, final CustomerRepository repository) {
        this.mongoTemplate = requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
        this.repository = requireNonNull(repository, "'repository' should not be null");
    }

    @Override
    public Customer create(final Customer customer) {
        try {
            return mongoTemplate
                    .insert(CustomerDocument.from(customer), CustomerDocument.COLLECTION)
                    .toDomain();
        } catch (final DuplicateKeyException e) {
            throw duplicateKey(e);
        }
    }

    @Override
    public void deleteById(final CustomerID id) {
        repository.deleteById(id.getValue());
    }

    @Override
    public Optional<Customer> findById(final CustomerID id) {
        return repository.findById(id.getValue()).map(CustomerDocument::toDomain);
    }

    @Override
    public Optional<Customer> findByEmail(final Email email) {
        return repository.findByEmail(email.getValue()).map(CustomerDocument::toDomain);
    }

    @Override
    public Customer update(final Customer customer) {
        try {
            return mongoTemplate
                    .save(CustomerDocument.from(customer), CustomerDocument.COLLECTION)
                    .toDomain();
        } catch (final DuplicateKeyException e) {
            throw duplicateKey(e);
        }
    }

    @Override
    public Pagination<Customer> findAll(final SearchQuery query) {
        final var mongoQuery = MongoGatewaySupport.searchQuery(query, "name", "cpf");
        return MongoGatewaySupport.paginate(
                mongoTemplate,
                mongoQuery,
                CustomerDocument.class,
                CustomerDocument.COLLECTION,
                query,
                SORTABLE_FIELDS,
                CustomerDocument::toDomain);
    }

    private static DomainException duplicateKey(final DuplicateKeyException e) {
        if (contains(e.getMessage(), "uq_customers_cpf")) {
            return new DomainException("'cpf' already in use");
        }
        if (contains(e.getMessage(), "uq_customers_email")) {
            return new DomainException("'email' already in use");
        }
        throw e;
    }
}
