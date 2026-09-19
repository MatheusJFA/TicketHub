package com.tickethub.infrastructure.shared.persistence;

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
        if (address == null) {
            return null;
        }
        return new AddressDocument(address.getStreet(), address.getNumber(), address.getComplement(),
                address.getNeighborhood(), address.getCity(), address.getState(), address.getCountry(),
                address.getZipCode());
    }

    public Address toDomain() {
        return Address.create(street, number, complement, neighborhood, city, state, country, zipCode);
    }
}
