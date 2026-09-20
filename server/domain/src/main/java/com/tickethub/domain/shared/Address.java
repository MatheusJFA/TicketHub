package com.tickethub.domain.shared;

import java.util.Objects;
import static java.util.Objects.isNull;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import com.tickethub.domain.ValueObject;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.geo.CepAddress;

public final class Address extends ValueObject {
    private static final String SPACE = " ";
    private final String street;
    private final String number;
    private final String complement;
    private final String neighborhood;
    private final String city;
    private final String state;
    private final String country;
    private final String zipCode;

    public Address(String street, String number, String complement, String neighborhood, String city, String state,
            String country, String zipCode) {
        this.street = required(street, "street");
        this.number = required(number, "number");
        this.complement = normalize(complement);
        this.neighborhood = required(neighborhood, "neighborhood");
        this.city = required(city, "city");
        this.state = required(state, "state");
        this.country = required(country, "country");
        this.zipCode = required(zipCode, "zipCode");
    }

    public static Address create(String street, String number, String complement, String neighborhood,
            String city, String state, String country, String zipCode) {
        return new Address(street, number, complement, neighborhood, city, state, country, zipCode);
    }

    /**
     * Returns a copy with street/neighborhood/city/state/country/zipCode taken
     * from the provider, keeping number and complement typed by the user.
     * Blank provider fields fall back to the current values.
     */
    public Address enrichedWith(final CepAddress cep) {
        Objects.requireNonNull(cep, "'cep' should not be null");
        return new Address(
                defaultIfBlank(cep.street(), street),
                number,
                complement,
                defaultIfBlank(cep.neighborhood(), neighborhood),
                defaultIfBlank(cep.city(), city),
                defaultIfBlank(cep.state(), state),
                defaultIfBlank(cep.country(), country),
                defaultIfBlank(cep.zipCode(), zipCode));
    }

    private static String required(String value, String field) {
        final String normalized = normalize(value);

        if (isNull(normalized) || normalized.isBlank()) {
            throw new DomainException("'" + field + "' should not be null or blank");
        }

        return normalized;
    }

    private static String normalize(String value) {
        if (isNull(value))
            return null;
        final String normalized = value.replaceAll("(?U)\\s+", SPACE).strip();
        return normalized.isEmpty() ? null : normalized;
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getComplement() {
        return complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getZipCode() {
        return zipCode;
    }

    @Override
    public String toString() {
        final String complementPart = isNull(complement) ? "" : ", " + complement;
        return "%s, %s%s - %s, %s - %s, %s, %s".formatted(
                street, number, complementPart, neighborhood, city, state, zipCode, country);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;

        if (!(other instanceof Address address))
            return false;

        return street.equals(address.street)
                && number.equals(address.number)
                && Objects.equals(complement, address.complement)
                && neighborhood.equals(address.neighborhood)
                && city.equals(address.city)
                && state.equals(address.state)
                && country.equals(address.country)
                && zipCode.equals(address.zipCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, number, complement, neighborhood, city, state, country, zipCode);
    }
}
