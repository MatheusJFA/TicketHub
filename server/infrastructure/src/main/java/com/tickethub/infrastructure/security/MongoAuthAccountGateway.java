package com.tickethub.infrastructure.security;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.tickethub.domain.authentication.AuthAccount;
import com.tickethub.domain.authentication.AuthAccountGateway;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.shared.Email;

@Component
public class MongoAuthAccountGateway implements AuthAccountGateway {

    private final CustomerGateway customers;
    private final PartnerGateway partners;
    private final SecurityProperties properties;

    public MongoAuthAccountGateway(final CustomerGateway customers, final PartnerGateway partners,
            final SecurityProperties properties) {
        this.customers = requireNonNull(customers, "'customers' should not be null");
        this.partners = requireNonNull(partners, "'partners' should not be null");
        this.properties = requireNonNull(properties, "'properties' should not be null");
    }

    @Override
    public Optional<AuthAccount> findByIdentifier(final String identifier) {
        final String normalized = trimToEmpty(identifier);
        return findCustomer(normalized)
                .or(() -> findPartner(normalized))
                // Bootstrap users keep working for operator access (e.g. ADMIN)
                // until an admin aggregate exists; matched by username.
                .or(() -> properties.findByUsername(normalized).map(this::fromBootstrap));
    }

    private Optional<AuthAccount> findCustomer(final String identifier) {
        return toEmail(identifier)
                .flatMap(customers::findByEmail)
                .filter(customer -> nonNull(customer.getPasswordHash()))
                .map(customer -> new AuthAccount(customer.getEmail().getValue(),
                        customer.getPasswordHash().getValue(),
                        new SecurityUser(customer.getEmail().getValue(), customer.getPasswordHash().getValue(),
                                Set.of(Role.CUSTOMER), customer.getId().getValue()).authorities(),
                        customer.getId().getValue()));
    }

    private Optional<AuthAccount> findPartner(final String identifier) {
        return toEmail(identifier)
                .flatMap(partners::findByEmail)
                .filter(partner -> nonNull(partner.getPasswordHash()))
                .map(partner -> new AuthAccount(partner.getEmail().getValue(),
                        partner.getPasswordHash().getValue(),
                        new SecurityUser(partner.getEmail().getValue(), partner.getPasswordHash().getValue(),
                                Set.of(Role.PARTNER), partner.getId().getValue()).authorities(),
                        partner.getId().getValue()));
    }

    private AuthAccount fromBootstrap(final SecurityUser user) {
        return new AuthAccount(user.username(), user.passwordHash(), user.authorities(), user.ownerId());
    }

    private static Optional<Email> toEmail(final String identifier) {
        try {
            return Optional.of(Email.create(identifier));
        } catch (final DomainException e) {
            return Optional.empty();
        }
    }
}
