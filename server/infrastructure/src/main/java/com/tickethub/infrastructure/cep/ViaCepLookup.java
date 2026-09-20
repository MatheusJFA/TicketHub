package com.tickethub.infrastructure.cep;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tickethub.domain.geo.CepAddress;
import com.tickethub.domain.geo.CepLookup;

@Component
public class ViaCepLookup implements CepLookup {

    private static final Logger LOG = LoggerFactory.getLogger(ViaCepLookup.class);

    private final ViaCepClient client;
    private final CepProperties properties;

    public ViaCepLookup(final ViaCepClient client, final CepProperties properties) {
        this.client = Objects.requireNonNull(client, "'client' should not be null");
        this.properties = Objects.requireNonNull(properties, "'properties' should not be null");
    }

    @Override
    public Optional<CepAddress> lookup(final String zipCode) {
        try {
            if (!properties.isEnabled()) {
                return Optional.empty();
            }
            return Optional.ofNullable(CepAddress.normalize(zipCode))
                    .flatMap(digits -> client.findByCep(digits)
                            .filter(response -> !response.hasError())
                            .map(response -> new CepAddress(digits, response.logradouro(),
                                    response.bairro(), response.localidade(), response.uf(),
                                    "Brasil")));
        } catch (final RuntimeException e) {
            // Fail-open contract: never break registration because of the provider.
            LOG.warn("CEP lookup failed zipCode={} error={}", zipCode, e.getMessage());
            return Optional.empty();
        }
    }
}
