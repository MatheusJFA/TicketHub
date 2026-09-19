package com.tickethub.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.shared.Email;

@ExtendWith(MockitoExtension.class)
class MongoAuthAccountGatewayTest {

    private static final String PASSWORD_HASH = "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS";

    @Mock
    private CustomerGateway customers;

    @Mock
    private PartnerGateway partners;

    private SecurityProperties properties;
    private MongoAuthAccountGateway gateway;

    @BeforeEach
    void setUp() {
        properties = new SecurityProperties();
        final var admin = new SecurityProperties.User();
        admin.setUsername("admin");
        admin.setPassword(new BCryptPasswordEncoder().encode("admin-local"));
        admin.setRoles(Set.of(Role.ADMIN));
        properties.getUsers().add(admin);
        gateway = new MongoAuthAccountGateway(customers, partners, properties);
    }

    @Test
    void givenCustomerEmail_whenFindByIdentifier_thenReturnsCustomerAccount() {
        final var customer = Customer.create("52998224725", "Maria Silva", "maria@domain.com", PASSWORD_HASH);
        when(customers.findByEmail(Email.create("maria@domain.com"))).thenReturn(Optional.of(customer));

        final var account = gateway.findByIdentifier("Maria@Domain.com").orElseThrow();

        assertEquals("maria@domain.com", account.subject());
        assertEquals(PASSWORD_HASH, account.passwordHash());
        assertEquals(customer.getId().getValue(), account.ownerId());
        assertTrue(account.authorities().contains("ROLE_CUSTOMER"));
        assertTrue(account.authorities().contains("customer:write"));
    }

    @Test
    void givenPartnerEmail_whenFindByIdentifier_thenReturnsPartnerAccount() {
        final var address = Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000");
        final var partner = Partner.create("Cinema Nova", "11222333000181", address, "cinema@domain.com",
                PASSWORD_HASH);
        when(customers.findByEmail(Email.create("cinema@domain.com"))).thenReturn(Optional.empty());
        when(partners.findByEmail(Email.create("cinema@domain.com"))).thenReturn(Optional.of(partner));

        final var account = gateway.findByIdentifier("cinema@domain.com").orElseThrow();

        assertEquals("cinema@domain.com", account.subject());
        assertEquals(partner.getId().getValue(), account.ownerId());
        assertTrue(account.authorities().contains("ROLE_PARTNER"));
        assertTrue(account.authorities().contains("show:create"));
    }

    @Test
    void givenBootstrapUsername_whenFindByIdentifier_thenReturnsBootstrapAccount() {
        final var account = gateway.findByIdentifier("admin").orElseThrow();

        assertEquals("admin", account.subject());
        assertTrue(account.authorities().contains("ROLE_ADMIN"));
        assertEquals(null, account.ownerId());
    }

    @Test
    void givenUnknownIdentifier_whenFindByIdentifier_thenReturnsEmpty() {
        when(customers.findByEmail(Email.create("ghost@domain.com"))).thenReturn(Optional.empty());
        when(partners.findByEmail(Email.create("ghost@domain.com"))).thenReturn(Optional.empty());

        assertTrue(gateway.findByIdentifier("ghost@domain.com").isEmpty());
    }
}
