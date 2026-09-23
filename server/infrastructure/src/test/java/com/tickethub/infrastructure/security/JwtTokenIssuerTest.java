package com.tickethub.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@DisplayName("JWT token issuer")
class JwtTokenIssuerTest {

    private static final String SECRET = "test-secret-key-with-at-least-32-bytes!!";

    private final SecretKey key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    private final SecurityProperties properties = properties();
    private final JwtTokenIssuer issuer =
            new JwtTokenIssuer(properties, new NimbusJwtEncoder(new ImmutableSecret<>(key)));
    private final JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();

    private static SecurityProperties properties() {
        final var properties = new SecurityProperties();
        properties.getJwt().setSecret(SECRET);
        properties.getJwt().setExpirationMinutes(60);
        return properties;
    }

    @Test
    @DisplayName("Given subject with authorities, when issue access, then returns signed token")
    void givenSubjectWithAuthorities_whenIssueAccess_thenReturnsSignedToken() {
        final var issued =
                issuer.issueAccess("maria@domain.com", List.of("ROLE_CUSTOMER", "customer:write"), "customer-1");

        assertEquals(3600, issued.expiresInSeconds());
        final var jwt = decoder.decode(issued.token());
        assertEquals("maria@domain.com", jwt.getSubject());
        assertEquals("tickethub", jwt.getClaimAsString("iss"));
        assertEquals("customer-1", jwt.getClaimAsString("ownerId"));
        final List<String> authorities = jwt.getClaimAsStringList("authorities");
        assertTrue(authorities.contains("ROLE_CUSTOMER"));
        assertTrue(authorities.contains("customer:write"));
    }

    @Test
    @DisplayName("Given subject without owner, when issue access, then omits owner id claim")
    void givenSubjectWithoutOwner_whenIssueAccess_thenOmitsOwnerIdClaim() {
        final var issued = issuer.issueAccess("admin", List.of("ROLE_ADMIN"), null);

        final var jwt = decoder.decode(issued.token());
        assertEquals("admin", jwt.getSubject());
        assertEquals(null, jwt.getClaimAsString("ownerId"));
    }
}
