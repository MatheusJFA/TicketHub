package com.tickethub.infrastructure.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tickethub.security")
public class SecurityProperties {

    private final Jwt jwt = new Jwt();
    private final List<User> users = new ArrayList<>();

    public Jwt getJwt() {
        return jwt;
    }

    public List<User> getUsers() {
        return users;
    }

    public Optional<SecurityUser> findByUsername(final String username) {
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .map(user -> new SecurityUser(user.getUsername(), user.getPassword(), user.getRoles()));
    }

    public static class Jwt {
        private String secret;
        private long expirationMinutes = 60;

        public String getSecret() {
            return secret;
        }

        public void setSecret(final String secret) {
            this.secret = secret;
        }

        public long getExpirationMinutes() {
            return expirationMinutes;
        }

        public void setExpirationMinutes(final long expirationMinutes) {
            this.expirationMinutes = expirationMinutes;
        }
    }

    public static class User {
        private String username;
        private String password;
        private Set<Role> roles = Set.of(Role.CUSTOMER);

        public String getUsername() {
            return username;
        }

        public void setUsername(final String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(final String password) {
            this.password = password;
        }

        public Set<Role> getRoles() {
            return roles;
        }

        public void setRoles(final Set<Role> roles) {
            this.roles = roles;
        }
    }
}
