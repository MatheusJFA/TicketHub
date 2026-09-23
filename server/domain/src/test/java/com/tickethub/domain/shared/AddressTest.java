package com.tickethub.domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.geography.ZipCodeAddress;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Address")
class AddressTest {
    private static final List<String> FIELDS =
            List.of("street", "number", "complement", "neighborhood", "city", "state", "country", "zipCode");

    @Test
    @DisplayName("Given padded fields, when create, then normalize and expose address")
    void givenPaddedFields_whenCreate_thenNormalizeAndExposeAddress() {
        final var address = Address.create(
                "  Rua   São João  ",
                "  12-A ",
                " Sala\t 10 ",
                " Centro ",
                " São\u00a0Paulo ",
                " SP ",
                " Brasil ",
                " 01035-000 ");

        assertEquals("Rua São João", address.getStreet());
        assertEquals("12-A", address.getNumber());
        assertEquals("Sala 10", address.getComplement());
        assertEquals("Centro", address.getNeighborhood());
        assertEquals("São Paulo", address.getCity());
        assertEquals("SP", address.getState());
        assertEquals("Brasil", address.getCountry());
        assertEquals("01035-000", address.getZipCode());
    }

    @ParameterizedTest
    @MethodSource("invalidRequiredFields")
    @DisplayName("Given missing required field, when create, then reject with field message")
    void givenMissingRequiredField_whenCreate_thenRejectWithFieldMessage(int index, String value) {
        final var fields = validFields();
        fields[index] = value;

        final var exception = assertThrows(DomainException.class, () -> create(fields));

        assertEquals("'" + FIELDS.get(index) + "' should not be null or blank", exception.getMessage());
    }

    private static Stream<Arguments> invalidRequiredFields() {
        return IntStream.range(0, FIELDS.size())
                .filter(index -> index != 2)
                .boxed()
                .flatMap(index -> Stream.of(null, "", " \t\n ", "\u00a0").map(value -> Arguments.of(index, value)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n", "\u00a0"})
    @DisplayName("Given missing complement, when create, then keep it optional")
    void givenMissingComplement_whenCreate_thenKeepItOptional(String complement) {
        final var fields = validFields();
        fields[2] = complement;

        assertNull(create(fields).getComplement());
    }

    @Test
    @DisplayName("Given equivalent addresses, when compare, then use normalized value equality")
    void givenEquivalentAddresses_whenCompare_thenUseNormalizedValueEquality() {
        final var first = create(validFields());
        final var fields = validFields();
        fields[0] = "  Rua   Augusta  ";
        final var second = create(fields);

        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(1, new HashSet<>(List.of(first, second)).size());
        assertNotEquals(null, first);
        assertNotEquals("Rua Augusta", first);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    @DisplayName("Given different field, when compare, then addresses differ")
    void givenDifferentField_whenCompare_thenAddressesDiffer(int index) {
        final var fields = validFields();
        fields[index] = "Another value";

        assertNotEquals(create(validFields()), create(fields));
    }

    @Test
    @DisplayName("Given international address, when create, then preserve postal format")
    void givenInternationalAddress_whenCreate_thenPreservePostalFormat() {
        final var address = Address.create(
                "Baker Street", "221B", null, "Marylebone", "London", "Greater London", "United Kingdom", "NW1 6XE");

        assertEquals("221B", address.getNumber());
        assertEquals("NW1 6XE", address.getZipCode());
    }

    @Test
    @DisplayName("Given invalid street, when call constructor, then cannot bypass validation")
    void givenInvalidStreet_whenCallConstructor_thenCannotBypassValidation() {
        final var exception = assertThrows(
                DomainException.class,
                () -> new Address(null, "100", null, "Centro", "São Paulo", "SP", "Brasil", "01305-000"));

        assertEquals("'street' should not be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Given ZIP code data, when enriched with, then overwrites except number and complement")
    void givenZipCodeData_whenEnrichedWith_thenOverwritesExceptNumberAndComplement() {
        final var address = create(validFields());
        final var zipCode =
                new ZipCodeAddress("01305000", "Avenida Paulista", "Bela Vista", "São Paulo", "SP", "Brasil");

        final var enriched = address.enrichedWith(zipCode);

        assertEquals("Avenida Paulista", enriched.getStreet());
        assertEquals("Bela Vista", enriched.getNeighborhood());
        assertEquals("100", enriched.getNumber());
        assertEquals("Sala 10", enriched.getComplement());
        assertEquals("01305000", enriched.getZipCode());
    }

    @Test
    @DisplayName("Given blank ZIP code fields, when enriched with, then keeps current values")
    void givenBlankZipCodeFields_whenEnrichedWith_thenKeepsCurrentValues() {
        final var address = create(validFields());
        final var zipCode = new ZipCodeAddress("01305000", "", null, "São Paulo", "", null);

        final var enriched = address.enrichedWith(zipCode);

        assertEquals("Rua Augusta", enriched.getStreet());
        assertEquals("Centro", enriched.getNeighborhood());
        assertEquals("São Paulo", enriched.getCity());
        assertEquals("SP", enriched.getState());
    }

    private static String[] validFields() {
        return new String[] {"Rua Augusta", "100", "Sala 10", "Centro", "São Paulo", "SP", "Brasil", "01305-000"};
    }

    private static Address create(String[] fields) {
        return Address.create(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6], fields[7]);
    }
}
