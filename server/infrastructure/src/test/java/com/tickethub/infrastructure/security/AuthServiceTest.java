package com.tickethub.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

class AuthServiceTest {

    private static final String SECRET = "test-secret-key-with-at-least-32-bytes!!";

    private final SecretKey key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    private final AuthService service = new AuthService(properties(),
            new BCryptPasswordEncoder(), new NimbusJwtEncoder(new ImmutableSecret<>(key)));
    private final JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();

    private static SecurityProperties properties() {
        final var properties = new SecurityProperties();
        final var admin = new SecurityProperties.User();
        admin.setUsername("admin");
        admin.setPassword(new BCryptPasswordEncoder().encode("admin-local"));
        admin.setRoles(Set.of(Role.ADMIN));
        properties.getUsers().add(admin);
        return properties;
    }

    @Test
    void givenValidCredentials_whenLogin_thenReturnsSignedToken() {
        final var response = service.login("admin", "admin-local");

        assertEquals("Bearer", response.tokenType());
        assertTrue(response.expiresIn() > 0);
        final var jwt = decoder.decode(response.token());
        assertEquals("admin", jwt.getSubject());
        assertEquals("tickethub", jwt.getClaimAsString("iss"));
        final List<String> authorities = jwt.getClaimAsStringList("authorities");
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("show:create"));
        assertTrue(authorities.contains("customer:delete"));
    }

    @Test
    void givenWrongPassword_whenLogin_thenReturns401() {
        final var exception = assertThrows(ResponseStatusException.class,
                () -> service.login("admin", "wrong"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void givenUnknownUser_whenLogin_thenReturns401() {
        final var exception = assertThrows(ResponseStatusException.class,
                () -> service.login("ghost", "admin-local"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}
