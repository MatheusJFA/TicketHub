package com.tickethub.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tickethub.infrastructure.api.AuditAPI;
import com.tickethub.infrastructure.api.CustomerAPI;
import com.tickethub.infrastructure.api.OperatorAPI;
import com.tickethub.infrastructure.api.PartnerAPI;
import com.tickethub.infrastructure.api.SectionAPI;
import com.tickethub.infrastructure.api.ShowAPI;
import com.tickethub.infrastructure.api.SpotAPI;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

@DisplayName("API authorization")
class ApiAuthorizationTest {

    // Filtra: autoridade em expressoes @PreAuthorize hasAuthority('X').
    private static final Pattern AUTHORITY = Pattern.compile("hasAuthority\\('([^']+)'\\)");
    // Filtra: chamadas de bean em expressoes @PreAuthorize (@bean.metodo().
    private static final Pattern BEAN_CALL = Pattern.compile("@([a-zA-Z]+)\\.([a-zA-Z]+)\\(");

    private static final Map<String, Set<String>> BEAN_METHODS = Map.of(
            "showAccess",
                    Set.of(
                            "canCreate",
                            "canWrite",
                            "canPublish",
                            "canDelete",
                            "canWriteSection",
                            "canPublishSection",
                            "canDeleteSection",
                            "canWriteSpot",
                            "canPublishSpot",
                            "canDeleteSpot"),
            "ownerAccess", Set.of("isSelfOrAdmin"));

    private static final Set<String> ROLES = Set.of("ADMIN");

    @Test
    @DisplayName("Every pre authorize expression uses known authorities beans and roles")
    void everyPreAuthorizeExpressionUsesKnownAuthoritiesBeansAndRoles() {
        final var knownAuthorities =
                Arrays.stream(Permission.values()).map(Permission::authority).collect(Collectors.toSet());
        final var unknown = new HashSet<String>();
        var expressions = 0;
        for (final Class<?> api : Set.of(
                AuditAPI.class,
                CustomerAPI.class,
                OperatorAPI.class,
                PartnerAPI.class,
                ShowAPI.class,
                SectionAPI.class,
                SpotAPI.class)) {
            for (final Method method : api.getDeclaredMethods()) {
                final PreAuthorize authorize = method.getAnnotation(PreAuthorize.class);
                if (authorize == null) {
                    continue;
                }
                expressions++;
                final String value = authorize.value();
                final Matcher authorities = AUTHORITY.matcher(value);
                while (authorities.find()) {
                    if (!knownAuthorities.contains(authorities.group(1))) {
                        unknown.add(authorities.group(1));
                    }
                }
                final Matcher beans = BEAN_CALL.matcher(value);
                while (beans.find()) {
                    final var allowed = BEAN_METHODS.get(beans.group(1));
                    if (allowed == null || !allowed.contains(beans.group(2))) {
                        unknown.add(beans.group(0));
                    }
                }
                if (value.contains("hasRole(")) {
                    // Filtra: papel em expressoes hasRole('X').
                    final var role = value.replaceAll(".*hasRole\\('([^']+)'\\).*", "$1");
                    if (!ROLES.contains(role)) {
                        unknown.add(role);
                    }
                }
            }
        }
        assertTrue(expressions > 0, "Expected @PreAuthorize expressions");
        assertTrue(unknown.isEmpty(), "Unknown authorities, beans or roles: " + unknown);
    }
}
