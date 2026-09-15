package com.tickethub.infrastructure.api.models;

import com.tickethub.domain.shared.Address;

public record AddressModel(String street, String number, String complement, String neighborhood,
                           String city, String state, String country, String zipCode) {
    public Address toDomain() {
        return Address.create(street, number, complement, neighborhood, city, state, country, zipCode);
    }

    public static AddressModel from(Address value) {
        return value == null ? null : new AddressModel(value.getStreet(), value.getNumber(), value.getComplement(),
                value.getNeighborhood(), value.getCity(), value.getState(), value.getCountry(), value.getZipCode());
    }
}
