package com.tickethub.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void givenAdmin_whenPermissions_thenHasAllPermissions() {
        assertEquals(EnumSet.allOf(Permission.class), Role.ADMIN.permissions());
    }

    @Test
    void givenPartner_whenPermissions_thenManagesCatalogButCannotDeleteAccounts() {
        final var permissions = Role.PARTNER.permissions();

        assertTrue(permissions.contains(Permission.SHOW_CREATE));
        assertTrue(permissions.contains(Permission.SHOW_PUBLISH));
        assertTrue(permissions.contains(Permission.SECTION_WRITE));
        assertTrue(permissions.contains(Permission.SPOT_DELETE));
        assertFalse(permissions.contains(Permission.CUSTOMER_DELETE));
        assertFalse(permissions.contains(Permission.PARTNER_DELETE));
    }

    @Test
    void givenCustomer_whenPermissions_thenCanOnlyWriteOwnProfile() {
        assertEquals(Set.of(Permission.CUSTOMER_WRITE), Role.CUSTOMER.permissions());
    }

    @Test
    void givenUserWithoutRoles_whenCreate_thenDefaultsToCustomer() {
        final var user = new SecurityUser("maria", "hash", null);

        assertEquals(Set.of(Role.CUSTOMER), user.roles());
    }

    @Test
    void givenPartnerUser_whenAuthorities_thenIncludesRoleAndPermissions() {
        final var user = new SecurityUser("cinema", "hash", Set.of(Role.PARTNER));

        assertTrue(user.authorities().contains("ROLE_PARTNER"));
        assertTrue(user.authorities().contains("show:create"));
        assertTrue(user.authorities().contains("spot:publish"));
        assertFalse(user.authorities().contains("customer:delete"));
    }
}
