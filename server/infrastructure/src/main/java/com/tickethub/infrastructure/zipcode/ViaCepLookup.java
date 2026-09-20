package com.tickethub.infrastructure.zipcode;

import static java.util.Objects.requireNonNull;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tickethub.domain.geography.ZipCodeAddress;
import com.tickethub.domain.geography.ZipCodeLookup;

@Component
public class ViaCepLookup implements ZipCodeLookup {

    private static final Logger LOG = LoggerFactory.getLogger(ViaCepLookup.class);

    private final ViaCepClient client;
    private final ZipCodeProperties properties;

    public ViaCepLookup(final ViaCepClient client, final ZipCodeProperties properties) {
        this.client = requireNonNull(client, "'client' should not be null");
        this.properties = requireNonNull(properties, "'properties' should not be null");
    }

    @Override
    public Optional<ZipCodeAddress> lookup(final String zipCode) {
        try {
            if (!properties.isEnabled()) {
                return Optional.empty();
            }
            return Optional.ofNullable(ZipCodeAddress.normalize(zipCode))
                    .flatMap(digits -> client.findByCep(digits)
                            .filter(response -> !response.hasError())
                            .map(response -> new ZipCodeAddress(digits, response.logradouro(),
                                    response.bairro(), response.localidade(), response.uf(),
                                    "Brasil")));
        } catch (final RuntimeException e) {
            // Fail-open contract: never break registration because of the provider.
            LOG.warn("CEP lookup failed zipCode={} error={}", zipCode, e.getMessage());
            return Optional.empty();
        }
    }
}
