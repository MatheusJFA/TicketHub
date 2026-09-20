package com.tickethub.infrastructure.partner;

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

import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.shared.Address;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;
import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.partner.PartnerMongoGateway;
import com.tickethub.infrastructure.partner.persistence.PartnerDocument;

@IntegrationTest
@DisplayName("Partner Mongo gateway")
class PartnerMongoGatewayIT extends ContainerSupport {

    private static final Address ADDRESS = Address.create("Rua Augusta", "100", "Sala 10", "Centro",
            "São Paulo", "SP", "Brasil", "01305-000");
    private static final String PASSWORD_HASH = "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PartnerMongoGateway gateway;

    @BeforeEach
    void cleanUp() {
        MongoCleanUpExtension.cleanCollections(mongoTemplate, PartnerDocument.COLLECTION);
    }

    @Test
    @DisplayName("Given a partner, when create, then persists and finds")
    void givenAPartner_whenCreate_thenPersistsAndFinds() {
        final var partner = Partner.create("Cinema Nova", "11222333000181", ADDRESS, "cinema@domain.com", PASSWORD_HASH);

        gateway.create(partner);

        final var found = gateway.findById(partner.getId());
        assertTrue(found.isPresent());
        assertEquals("Cinema Nova", found.get().getName().getValue());
        assertEquals("11222333000181", found.get().getCnpj().getValue());
        assertEquals("cinema@domain.com", found.get().getEmail().getValue());
        assertEquals(PASSWORD_HASH, found.get().getPasswordHash().getValue());
        assertEquals("Rua Augusta", found.get().getAddress().getStreet());
        assertEquals("Sala 10", found.get().getAddress().getComplement());
        assertNotNull(found.get().getCreatedAt());
    }

    @Test
    @DisplayName("Given duplicate CNPJ, when create, then throws domain exception")
    void givenDuplicateCnpj_whenCreate_thenThrowsDomainException() {
        gateway.create(Partner.create("Cinema Nova", "11222333000181", ADDRESS, "cinema@domain.com", PASSWORD_HASH));

        final var exception = assertThrows(DomainException.class,
                () -> gateway.create(Partner.create("Cinema Velha", "11222333000181", ADDRESS, "velha@domain.com", PASSWORD_HASH)));

        assertEquals("'cnpj' already in use", exception.getMessage());
    }

    @Test
    @DisplayName("Given duplicate email, when create, then throws domain exception")
    void givenDuplicateEmail_whenCreate_thenThrowsDomainException() {
        gateway.create(Partner.create("Cinema Nova", "11222333000181", ADDRESS, "cinema@domain.com", PASSWORD_HASH));

        final var exception = assertThrows(DomainException.class,
                () -> gateway.create(Partner.create("Cinema Velha", "04252011000110", ADDRESS, "cinema@domain.com", PASSWORD_HASH)));

        assertEquals("'email' already in use", exception.getMessage());
    }

    @Test
    @DisplayName("Given a partner, when find by email, then returns partner")
    void givenAPartner_whenFindByEmail_thenReturnsPartner() {
        final var partner = gateway.create(
                Partner.create("Cinema Nova", "11222333000181", ADDRESS, "cinema@domain.com", PASSWORD_HASH));

        final var found = gateway.findByEmail(com.tickethub.domain.shared.Email.create("CINEMA@domain.com"));

        assertTrue(found.isPresent());
        assertEquals(partner.getId(), found.get().getId());
    }

    @Test
    @DisplayName("Given a partner, when update, then persists changes")
    void givenAPartner_whenUpdate_thenPersistsChanges() {
        final var partner = gateway.create(Partner.create("Cinema Nova", "11222333000181", ADDRESS, "cinema@domain.com", PASSWORD_HASH));

        partner.changeName("Cinema Novo");
        gateway.update(partner);

        assertEquals("Cinema Novo", gateway.findById(partner.getId()).orElseThrow().getName().getValue());
    }

    @Test
    @DisplayName("Given a partner, when delete, then removes")
    void givenAPartner_whenDelete_thenRemoves() {
        final var partner = gateway.create(Partner.create("Cinema Nova", "11222333000181", ADDRESS, "cinema@domain.com", PASSWORD_HASH));

        gateway.deleteById(partner.getId());

        assertFalse(gateway.findById(partner.getId()).isPresent());
    }

    @Test
    @DisplayName("Given partners, when find all, then searches")
    void givenPartners_whenFindAll_thenSearches() {
        gateway.create(Partner.create("Cinema Nova", "11222333000181", ADDRESS, "cinema@domain.com", PASSWORD_HASH));
        gateway.create(Partner.create("Teatro Velho", "04252011000110", ADDRESS, "teatro@domain.com", PASSWORD_HASH));

        final var search = gateway.findAll(new SearchQuery(0, 10, "cinema", "name", "asc"));

        assertEquals(1, search.totalItems());
        assertEquals("Cinema Nova", search.items().get(0).getName().getValue());
        assertFalse(gateway.findById(PartnerID.generate()).isPresent());
    }
}
