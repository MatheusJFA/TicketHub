package com.tickethub.domain.core.partner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.shared.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Partner")
class PartnerTest {

    private static final String VALID_EMAIL = "cinema@domain.com";
    private static final String VALID_HASH = "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS";

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "J"})
    @DisplayName("Given invalid name, when create, then throw domain exception for null and blank")
    void givenInvalidName_whenCreate_thenThrowDomainExceptionForNullAndBlank(String name) {
        assertEquals(
                "Invalid name " + name,
                assertThrows(
                                DomainException.class,
                                () -> Partner.create(
                                        name,
                                        "11222333000181",
                                        Address.create(
                                                "Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                                        VALID_EMAIL,
                                        VALID_HASH))
                        .getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "123"})
    @DisplayName("Given invalid CNPJ, when create, then throw domain exception for null and blank")
    void givenInvalidCnpj_whenCreate_thenThrowDomainExceptionForNullAndBlank(String cnpj) {
        assertEquals(
                "Invalid CNPJ",
                assertThrows(
                                DomainException.class,
                                () -> Partner.create(
                                        "Cinema Nova",
                                        cnpj,
                                        Address.create(
                                                "Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                                        VALID_EMAIL,
                                        VALID_HASH))
                        .getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "plainaddress"})
    @DisplayName("Given invalid email, when create, then throw domain exception")
    void givenInvalidEmail_whenCreate_thenThrowDomainException(String email) {
        assertEquals(
                "Invalid email",
                assertThrows(
                                DomainException.class,
                                () -> Partner.create(
                                        "Cinema Nova",
                                        "11222333000181",
                                        Address.create(
                                                "Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                                        email,
                                        VALID_HASH))
                        .getMessage());
    }

    @Test
    @DisplayName("Given valid params, when create, then instantiate partner")
    void givenValidParams_whenCreate_thenInstantiatePartner() {
        final var expectedName = "Cinema Nova";
        final var expectedCnpj = "11222333000181";

        final var actualPartner = Partner.create(
                expectedName,
                expectedCnpj,
                Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                VALID_EMAIL,
                VALID_HASH);

        assertNotNull(actualPartner);
        assertNotNull(actualPartner.getId());
        assertEquals(expectedName, actualPartner.getName().getValue());
        assertEquals(expectedCnpj, actualPartner.getCnpj().getValue());
        assertEquals(VALID_EMAIL, actualPartner.getEmail().getValue());
        assertEquals(VALID_HASH, actualPartner.getPasswordHash().getValue());
        assertEquals(PartnerStatus.PENDING, actualPartner.getStatus());
    }

    @Test
    @DisplayName("Given pending partner, when approve, then status is active")
    void givenPendingPartner_whenApprove_thenStatusIsActive() {
        final var partner = Partner.create(
                "Cinema Nova",
                "11222333000181",
                Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                VALID_EMAIL,
                VALID_HASH);

        partner.approve();

        assertEquals(PartnerStatus.ACTIVE, partner.getStatus());
    }

    @Test
    @DisplayName("Given pending partner, when reject, then status is rejected")
    void givenPendingPartner_whenReject_thenStatusIsRejected() {
        final var partner = Partner.create(
                "Cinema Nova",
                "11222333000181",
                Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                VALID_EMAIL,
                VALID_HASH);

        partner.reject();

        assertEquals(PartnerStatus.REJECTED, partner.getStatus());
    }

    @Test
    @DisplayName("Given non pending partner, when approve or reject, then throw domain exception")
    void givenNonPendingPartner_whenApproveOrReject_thenThrowDomainException() {
        final var partner = Partner.create(
                "Cinema Nova",
                "11222333000181",
                Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                VALID_EMAIL,
                VALID_HASH);
        partner.approve();

        assertEquals(
                "Only pending partners can be approved",
                assertThrows(DomainException.class, partner::approve).getMessage());
        assertEquals(
                "Only pending partners can be rejected",
                assertThrows(DomainException.class, partner::reject).getMessage());
    }

    @Test
    @DisplayName("Given invalid name, when create, then throw domain exception")
    void givenInvalidName_whenCreate_thenThrowDomainException() {
        final var expectedName = "J";
        final var expectedCnpj = "11222333000181";

        DomainException exception = assertThrows(
                DomainException.class,
                () -> Partner.create(
                        expectedName,
                        expectedCnpj,
                        Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                        VALID_EMAIL,
                        VALID_HASH));

        assertEquals("Invalid name " + expectedName, exception.getMessage());
    }

    @Test
    @DisplayName("Given invalid CNPJ, when create, then throw domain exception")
    void givenInvalidCnpj_whenCreate_thenThrowDomainException() {
        final var expectedName = "Cinema Nova";
        final var expectedCnpj = "11222333000182";

        DomainException exception = assertThrows(
                DomainException.class,
                () -> Partner.create(
                        expectedName,
                        expectedCnpj,
                        Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                        VALID_EMAIL,
                        VALID_HASH));

        assertEquals("Invalid CNPJ", exception.getMessage());
    }

    @Test
    @DisplayName("Given create, when read webhook, then null")
    void givenCreate_whenReadWebhook_thenNull() {
        final var partner = Partner.create(
                "Cinema Nova",
                "11222333000181",
                Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                VALID_EMAIL,
                VALID_HASH);

        assertEquals(null, partner.getWebhookUrl());
        assertEquals(null, partner.getWebhookSecret());
    }

    @Test
    @DisplayName("Given urls, when change webhook, then updates")
    void givenUrls_whenChangeWebhook_thenUpdates() {
        final var partner = Partner.create(
                "Cinema Nova",
                "11222333000181",
                Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"),
                VALID_EMAIL,
                VALID_HASH);

        partner.changeWebhook("https://partner.domain.com/hook", "s3cr3t");

        assertEquals("https://partner.domain.com/hook", partner.getWebhookUrl());
        assertEquals("s3cr3t", partner.getWebhookSecret());
    }
}
