package com.tickethub.infrastructure.partner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class PartnerMongoGatewayIT extends ContainerSupport {

    private static final Address ADDRESS = Address.create("Rua Augusta", "100", "Sala 10", "Centro",
            "São Paulo", "SP", "Brasil", "01305-000");

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PartnerMongoGateway gateway;

    @BeforeEach
    void cleanUp() {
        MongoCleanUpExtension.cleanCollections(mongoTemplate, PartnerDocument.COLLECTION);
    }

    @Test
    void givenAPartner_whenCreate_thenPersistsAndFinds() {
        final var partner = Partner.create("Cinema Nova", "11222333000181", ADDRESS);

        gateway.create(partner);

        final var found = gateway.findById(partner.getId());
        assertTrue(found.isPresent());
        assertEquals("Cinema Nova", found.get().getName().getValue());
        assertEquals("11222333000181", found.get().getCnpj().getValue());
        assertEquals("Rua Augusta", found.get().getAddress().getStreet());
        assertEquals("Sala 10", found.get().getAddress().getComplement());
        assertNotNull(found.get().getCreatedAt());
    }

    @Test
    void givenDuplicateCnpj_whenCreate_thenThrowsDomainException() {
        gateway.create(Partner.create("Cinema Nova", "11222333000181", ADDRESS));

        final var exception = assertThrows(DomainException.class,
                () -> gateway.create(Partner.create("Cinema Velha", "11222333000181", ADDRESS)));

        assertEquals("'cnpj' already in use", exception.getMessage());
    }

    @Test
    void givenAPartner_whenUpdate_thenPersistsChanges() {
        final var partner = gateway.create(Partner.create("Cinema Nova", "11222333000181", ADDRESS));

        partner.changeName("Cinema Novo");
        gateway.update(partner);

        assertEquals("Cinema Novo", gateway.findById(partner.getId()).orElseThrow().getName().getValue());
    }

    @Test
    void givenAPartner_whenDelete_thenRemoves() {
        final var partner = gateway.create(Partner.create("Cinema Nova", "11222333000181", ADDRESS));

        gateway.deleteById(partner.getId());

        assertFalse(gateway.findById(partner.getId()).isPresent());
    }

    @Test
    void givenPartners_whenFindAll_thenSearches() {
        gateway.create(Partner.create("Cinema Nova", "11222333000181", ADDRESS));
        gateway.create(Partner.create("Teatro Velho", "04252011000110", ADDRESS));

        final var search = gateway.findAll(new SearchQuery(0, 10, "cinema", "name", "asc"));

        assertEquals(1, search.totalItems());
        assertEquals("Cinema Nova", search.items().get(0).getName().getValue());
        assertFalse(gateway.findById(PartnerID.generate()).isPresent());
    }
}
