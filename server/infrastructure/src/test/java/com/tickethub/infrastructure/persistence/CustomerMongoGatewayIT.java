package com.tickethub.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;
import com.tickethub.infrastructure.MongoCleanUpExtension;

@IntegrationTest
class CustomerMongoGatewayIT extends ContainerSupport {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CustomerMongoGateway gateway;

    @BeforeEach
    void cleanUp() {
        MongoCleanUpExtension.cleanCollections(mongoTemplate, CustomerDocument.COLLECTION);
    }

    @Test
    void givenACustomer_whenCreate_thenPersistsAndFinds() {
        final var customer = Customer.create("52998224725", "Maria Silva");

        final var saved = gateway.create(customer);

        assertEquals(customer.getId(), saved.getId());
        final var found = gateway.findById(customer.getId());
        assertTrue(found.isPresent());
        assertEquals("52998224725", found.get().getCpf().getValue());
        assertEquals("Maria Silva", found.get().getName().getValue());
        assertNotNull(found.get().getCreatedAt());
        assertNotNull(found.get().getUpdatedAt());
    }

    @Test
    void givenDuplicateCpf_whenCreate_thenThrowsDomainException() {
        gateway.create(Customer.create("52998224725", "Maria Silva"));

        final var exception = assertThrows(DomainException.class,
                () -> gateway.create(Customer.create("52998224725", "Maria Souza")));

        assertEquals("'cpf' already in use", exception.getMessage());
    }

    @Test
    void givenACustomer_whenUpdate_thenPersistsChanges() {
        final var customer = gateway.create(Customer.create("52998224725", "Maria Silva"));

        customer.changeName("Maria Souza");
        final var updated = gateway.update(customer);

        assertEquals("Maria Souza", updated.getName().getValue());
        assertEquals("Maria Souza", gateway.findById(customer.getId()).orElseThrow().getName().getValue());
    }

    @Test
    void givenACustomer_whenDelete_thenRemoves() {
        final var customer = gateway.create(Customer.create("52998224725", "Maria Silva"));

        gateway.deleteById(customer.getId());

        assertFalse(gateway.findById(customer.getId()).isPresent());
        gateway.deleteById(customer.getId());
    }

    @Test
    void givenCustomers_whenFindAll_thenPaginatesAndSearches() {
        gateway.create(Customer.create("52998224725", "Maria Silva"));
        gateway.create(Customer.create("12345678909", "João Souza"));

        final var first = gateway.findAll(new SearchQuery(0, 1, "", "name", "asc"));
        assertEquals(0, first.currentPage());
        assertEquals(1, first.perPage());
        assertEquals(2, first.totalItems());
        assertEquals(1, first.items().size());

        final var second = gateway.findById(first.items().get(0).getId());
        assertTrue(second.isPresent());

        final var search = gateway.findAll(new SearchQuery(0, 10, "maria", "name", "asc"));
        assertEquals(1, search.totalItems());
        assertEquals("Maria Silva", search.items().get(0).getName().getValue());

        final var fallback = gateway.findAll(new SearchQuery(0, 10, "", "unknown-field", "desc"));
        assertEquals(2, fallback.totalItems());
    }

    @Test
    void givenMissingCustomer_whenFindById_thenReturnsEmpty() {
        assertFalse(gateway
                .findById(com.tickethub.domain.core.customer.CustomerID.generate()).isPresent());
    }
}
