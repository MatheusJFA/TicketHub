package com.tickethub.infrastructure.configuration;

import static java.util.Objects.isNull;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.tickethub.domain.authentication.RevokedAccessTokenGateway;
import com.tickethub.infrastructure.authentication.AuthSessionProperties;
import com.tickethub.infrastructure.security.GeneratedSecrets;
import com.tickethub.infrastructure.security.RevokedAccessTokenFilter;
import com.tickethub.infrastructure.security.SecurityProperties;
import com.tickethub.infrastructure.web.CheckoutProperties;
import com.tickethub.infrastructure.web.CorsProperties;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity(proxyTargetClass = true)
@EnableConfigurationProperties({
    SecurityProperties.class,
    AuthSessionProperties.class,
    CorsProperties.class,
    CheckoutProperties.class
})
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(
            final HttpSecurity http,
            final JwtDecoder jwtDecoder,
            final JwtAuthenticationConverter jwtAuthenticationConverter,
            final CorsConfigurationSource corsConfigurationSource,
            final RevokedAccessTokenFilter revokedAccessTokenFilter)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/logout",
                                "/actuator/health",
                                "/actuator/prometheus",
                                "/payments/webhook",
                                "/payments/mercadopago",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/shows/**", "/sections/**", "/spots/**", "/zipcode/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/customers/**", "/partners")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(
                        jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .addFilterAfter(
                        revokedAccessTokenFilter,
                        org.springframework.security.oauth2.server.resource.web.authentication
                                .BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public GeneratedSecrets generatedSecrets(
            @Value("${tickethub.security.jwt.secret:}") final String jwtSecret,
            @Value("${tickethub.tickets.signature-secret:}") final String ticketSecret,
            final ObjectProvider<MongoTemplate> mongo) {
        return new GeneratedSecrets(jwtSecret, ticketSecret, mongo.getIfAvailable());
    }

    @Bean
    public RevokedAccessTokenFilter revokedAccessTokenFilter(final ObjectProvider<RevokedAccessTokenGateway> gateways) {
        // Slice tests (WebMvcTest) have no Redis adapter: fall back to no-op.
        return new RevokedAccessTokenFilter(gateways.getIfAvailable(NoOpRevokedAccessTokenGateway::new));
    }

    static final class NoOpRevokedAccessTokenGateway implements RevokedAccessTokenGateway {
        @Override
        public void revoke(final String tokenId, final Instant expiresAt) {}

        @Override
        public boolean isRevoked(final String tokenId) {
            return false;
        }
    }

    @Bean
    public JwtDecoder jwtDecoder(final GeneratedSecrets secrets) {
        return NimbusJwtDecoder.withSecretKey(secretKey(secrets)).build();
    }

    @Bean
    public JwtEncoder jwtEncoder(final GeneratedSecrets secrets) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(secrets)));
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        final var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> jwt.getClaimAsStringList("authorities").stream()
                .map(authority -> (GrantedAuthority) new SimpleGrantedAuthority(authority))
                .toList());
        return converter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(final CorsProperties properties) {
        final var source = new UrlBasedCorsConfigurationSource();
        final var config = new CorsConfiguration();
        config.setAllowedOrigins(properties.getAllowedOrigins());
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Idempotency-Key"));
        config.setExposedHeaders(java.util.List.of("Location"));
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static SecretKey secretKey(final GeneratedSecrets secrets) {
        final String secret = secrets.jwtSecret();
        if (isNull(secret) || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("tickethub.security.jwt.secret must be at least 32 bytes long");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
