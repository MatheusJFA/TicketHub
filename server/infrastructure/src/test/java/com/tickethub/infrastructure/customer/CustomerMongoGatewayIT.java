package com.tickethub.infrastructure.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;
import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.customer.CustomerMongoGateway;
import com.tickethub.infrastructure.customer.persistence.CustomerDocument;

@IntegrationTest
@DisplayName("Customer Mongo gateway")
class CustomerMongoGatewayIT extends ContainerSupport {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CustomerMongoGateway gateway;

    private static final String PASSWORD_HASH = "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS";

    @BeforeEach
    void cleanUp() {
        MongoCleanUpExtension.cleanCollections(mongoTemplate, CustomerDocument.COLLECTION);
    }

    @Test
    @DisplayName("Given a customer, when create, then persists and finds")
    void givenACustomer_whenCreate_thenPersistsAndFinds() {
        final var customer = Customer.create("52998224725", "Maria Silva", "maria@domain.com", PASSWORD_HASH);

        final var saved = gateway.create(customer);

        assertEquals(customer.getId(), saved.getId());
        final var found = gateway.findById(customer.getId());
        assertTrue(found.isPresent());
        assertEquals("52998224725", found.get().getCpf().getValue());
        assertEquals("Maria Silva", found.get().getName().getValue());
        assertEquals("maria@domain.com", found.get().getEmail().getValue());
        assertEquals(PASSWORD_HASH, found.get().getPasswordHash().getValue());
        assertNotNull(found.get().getCreatedAt());
        assertNotNull(found.get().getUpdatedAt());
    }

    @Test
    @DisplayName("Given duplicate CPF, when create, then throws domain exception")
    void givenDuplicateCpf_whenCreate_thenThrowsDomainException() {
        gateway.create(Customer.create("52998224725", "Maria Silva", "maria@domain.com", PASSWORD_HASH));

        final var exception = assertThrows(DomainException.class,
                () -> gateway.create(Customer.create("52998224725", "Maria Souza", "maria.souza@domain.com", PASSWORD_HASH)));

        assertEquals("'cpf' already in use", exception.getMessage());
    }

    @Test
    @DisplayName("Given duplicate email, when create, then throws domain exception")
    void givenDuplicateEmail_whenCreate_thenThrowsDomainException() {
        gateway.create(Customer.create("52998224725", "Maria Silva", "maria@domain.com", PASSWORD_HASH));

        final var exception = assertThrows(DomainException.class,
                () -> gateway.create(Customer.create("12345678909", "Maria Souza", "maria@domain.com", PASSWORD_HASH)));

        assertEquals("'email' already in use", exception.getMessage());
    }

    @Test
    @DisplayName("Given a customer, when find by email, then returns customer")
    void givenACustomer_whenFindByEmail_thenReturnsCustomer() {
        final var customer = gateway.create(
                Customer.create("52998224725", "Maria Silva", "maria@domain.com", PASSWORD_HASH));

        final var found = gateway.findByEmail(com.tickethub.domain.shared.Email.create("MARIA@domain.com"));

        assertTrue(found.isPresent());
        assertEquals(customer.getId(), found.get().getId());
    }

    @Test
    @DisplayName("Given a customer, when update, then persists changes")
    void givenACustomer_whenUpdate_thenPersistsChanges() {
        final var customer = gateway.create(Customer.create("52998224725", "Maria Silva", "maria@domain.com", PASSWORD_HASH));

        customer.changeName("Maria Souza");
        final var updated = gateway.update(customer);

        assertEquals("Maria Souza", updated.getName().getValue());
        assertEquals("Maria Souza", gateway.findById(customer.getId()).orElseThrow().getName().getValue());
    }

    @Test
    @DisplayName("Given a customer, when delete, then removes")
    void givenACustomer_whenDelete_thenRemoves() {
        final var customer = gateway.create(Customer.create("52998224725", "Maria Silva", "maria@domain.com", PASSWORD_HASH));

        gateway.deleteById(customer.getId());

        assertFalse(gateway.findById(customer.getId()).isPresent());
        gateway.deleteById(customer.getId());
    }

    @Test
    @DisplayName("Given customers, when find all, then paginates and searches")
    void givenCustomers_whenFindAll_thenPaginatesAndSearches() {
        gateway.create(Customer.create("52998224725", "Maria Silva", "maria@domain.com", PASSWORD_HASH));
        gateway.create(Customer.create("12345678909", "João Souza", "joao@domain.com", PASSWORD_HASH));

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
    @DisplayName("Given missing customer, when find by id, then returns empty")
    void givenMissingCustomer_whenFindById_thenReturnsEmpty() {
        assertFalse(gateway
                .findById(com.tickethub.domain.core.customer.CustomerID.generate()).isPresent());
    }
}
