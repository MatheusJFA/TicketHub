package com.tickethub.infrastructure.customer;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.pagination.Pagination;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.infrastructure.customer.persistence.CustomerDocument;
import com.tickethub.infrastructure.shared.persistence.MongoGatewaySupport;

@Component
public class CustomerMongoGateway implements CustomerGateway {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "cpf", "createdAt", "updatedAt");

    private final MongoTemplate mongoTemplate;

    public CustomerMongoGateway(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
    }

    @Override
    public Customer create(final Customer customer) {
        try {
            return mongoTemplate.insert(CustomerDocument.from(customer), CustomerDocument.COLLECTION).toDomain();
        } catch (final DuplicateKeyException e) {
            throw duplicateKey(e);
        }
    }

    @Override
    public void deleteById(final CustomerID id) {
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(id.getValue())),
                CustomerDocument.class, CustomerDocument.COLLECTION);
    }

    @Override
    public Optional<Customer> findById(final CustomerID id) {
        return Optional
                .ofNullable(mongoTemplate.findById(id.getValue(), CustomerDocument.class,
                        CustomerDocument.COLLECTION))
                .map(CustomerDocument::toDomain);
    }

    @Override
    public Customer update(final Customer customer) {
        try {
            return mongoTemplate.save(CustomerDocument.from(customer), CustomerDocument.COLLECTION).toDomain();
        } catch (final DuplicateKeyException e) {
            throw duplicateKey(e);
        }
    }

    @Override
    public Pagination<Customer> findAll(final SearchQuery query) {
        final var mongoQuery = MongoGatewaySupport.searchQuery(query, "name", "cpf");
        return MongoGatewaySupport.paginate(mongoTemplate, mongoQuery, CustomerDocument.class,
                CustomerDocument.COLLECTION, query, SORTABLE_FIELDS, CustomerDocument::toDomain);
    }

    private static DomainException duplicateKey(final DuplicateKeyException e) {
        if (e.getMessage() != null && e.getMessage().contains("uq_customers_cpf")) {
            return new DomainException("'cpf' already in use");
        }
        throw e;
    }
}
