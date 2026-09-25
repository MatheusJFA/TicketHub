package com.tickethub.infrastructure.security;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import com.tickethub.domain.authentication.AuthAccount;
import com.tickethub.domain.authentication.AuthAccountGateway;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.operator.OperatorGateway;
import com.tickethub.domain.core.partner.PartnerGateway;
import com.tickethub.domain.core.partner.PartnerStatus;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.shared.Email;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class MongoAuthAccountGateway implements AuthAccountGateway {

    private final CustomerGateway customers;
    private final PartnerGateway partners;
    private final OperatorGateway operators;

    public MongoAuthAccountGateway(
            final CustomerGateway customers, final PartnerGateway partners, final OperatorGateway operators) {
        this.customers = requireNonNull(customers, "'customers' should not be null");
        this.partners = requireNonNull(partners, "'partners' should not be null");
        this.operators = requireNonNull(operators, "'operators' should not be null");
    }

    @Override
    public Optional<AuthAccount> findByIdentifier(final String identifier) {
        final String normalized = trimToEmpty(identifier);
        return findCustomer(normalized).or(() -> findPartner(normalized)).or(() -> findOperator(normalized));
    }

    private Optional<AuthAccount> findCustomer(final String identifier) {
        return toEmail(identifier)
                .flatMap(customers::findByEmail)
                .filter(customer -> nonNull(customer.getPasswordHash()))
                .map(customer -> new AuthAccount(
                        customer.getEmail().getValue(),
                        customer.getPasswordHash().getValue(),
                        new SecurityUser(
                                        customer.getEmail().getValue(),
                                        customer.getPasswordHash().getValue(),
                                        Set.of(Role.CUSTOMER),
                                        customer.getId().getValue())
                                .authorities(),
                        customer.getId().getValue()));
    }

    private Optional<AuthAccount> findPartner(final String identifier) {
        return toEmail(identifier)
                .flatMap(partners::findByEmail)
                .filter(partner -> nonNull(partner.getPasswordHash()))
                // Pending/rejected partners cannot login until approved by an admin.
                .filter(partner -> partner.getStatus() == PartnerStatus.ACTIVE)
                .map(partner -> new AuthAccount(
                        partner.getEmail().getValue(),
                        partner.getPasswordHash().getValue(),
                        new SecurityUser(
                                        partner.getEmail().getValue(),
                                        partner.getPasswordHash().getValue(),
                                        Set.of(Role.PARTNER),
                                        partner.getId().getValue())
                                .authorities(),
                        partner.getId().getValue()));
    }

    private Optional<AuthAccount> findOperator(final String identifier) {
        return toEmail(identifier)
                .flatMap(operators::findByEmail)
                .filter(operator -> nonNull(operator.getPasswordHash()))
                .map(operator -> new AuthAccount(
                        operator.getEmail().getValue(),
                        operator.getPasswordHash().getValue(),
                        new SecurityUser(
                                        operator.getEmail().getValue(),
                                        operator.getPasswordHash().getValue(),
                                        Set.of(Role.ADMIN),
                                        null)
                                .authorities(),
                        null));
    }

    private static Optional<Email> toEmail(final String identifier) {
        try {
            return Optional.of(Email.create(identifier));
        } catch (final DomainException e) {
            return Optional.empty();
        }
    }
}
