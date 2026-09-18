package com.tickethub.infrastructure.api.models;

public record AddressModel(String street, String number, String complement, String neighborhood,
                           String city, String state, String country, String zipCode) {
}
