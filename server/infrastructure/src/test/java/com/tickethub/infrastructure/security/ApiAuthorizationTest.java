package com.tickethub.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import com.tickethub.infrastructure.api.CustomerAPI;
import com.tickethub.infrastructure.api.PartnerAPI;
import com.tickethub.infrastructure.api.SectionAPI;
import com.tickethub.infrastructure.api.ShowAPI;
import com.tickethub.infrastructure.api.SpotAPI;

class ApiAuthorizationTest {

    private static final Pattern AUTHORITY = Pattern.compile("hasAuthority\\('([^']+)'\\)");

    @Test
    void everyAuthorityUsedInPreAuthorizeExistsInPermissions() {
        final var known = Arrays.stream(Permission.values())
                .map(Permission::authority)
                .collect(Collectors.toSet());
        final var used = new HashSet<String>();
        for (final Class<?> api : Set.of(CustomerAPI.class, PartnerAPI.class, ShowAPI.class,
                SectionAPI.class, SpotAPI.class)) {
            for (final Method method : api.getDeclaredMethods()) {
                final PreAuthorize authorize = method.getAnnotation(PreAuthorize.class);
                if (authorize == null) {
                    continue;
                }
                final Matcher matcher = AUTHORITY.matcher(authorize.value());
                assertTrue(matcher.find(),
                        "Unsupported expression in " + api.getSimpleName() + "#" + method.getName());
                used.add(matcher.group(1));
            }
        }
        final var unknown = used.stream().filter(authority -> !known.contains(authority)).toList();
        assertTrue(unknown.isEmpty(), "Unknown authorities: " + unknown);
        assertTrue(used.contains("show:create"));
    }
}
