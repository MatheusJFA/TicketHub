package com.tickethub.infrastructure.shared.persistence;

import java.util.Optional;

import com.tickethub.domain.shared.Address;

public record AddressDocument(
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String country,
        String zipCode) {

    public static AddressDocument from(final Address address) {
        return Optional.ofNullable(address)
                .map(current -> new AddressDocument(current.getStreet(), current.getNumber(),
                        current.getComplement(), current.getNeighborhood(), current.getCity(),
                        current.getState(), current.getCountry(), current.getZipCode()))
                .orElse(null);
    }

    public Address toDomain() {
        return Address.create(street, number, complement, neighborhood, city, state, country, zipCode);
    }
}
