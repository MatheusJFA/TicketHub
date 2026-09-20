package com.tickethub.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.DisplayName;

import com.tickethub.domain.Identifier;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.core.spot.SpotID;

@DisplayName("Identifier")
class IdentifierTest {
    private static final String VALUE = "cf4361d9-8e93-4c53-9ac5-629680d98469";
    private static final String OTHER_VALUE = "f2f50ac2-ef1e-42a5-89c1-b0527662b458";

    static Stream<Function<String, Identifier>> identifierFactories() {
        return Stream.<Function<String, Identifier>>of(
                CustomerID::from, PartnerID::from, SectionID::from, ShowID::from, SpotID::from);
    }

    @ParameterizedTest
    @MethodSource("identifierFactories")
    @DisplayName("Given same type and value, when compare, then be equal and have same hash code")
    void givenSameTypeAndValue_whenCompare_thenBeEqualAndHaveSameHashCode(Function<String, Identifier> factory) {
        Identifier first = factory.apply(VALUE);
        Identifier second = factory.apply(VALUE);
        Identifier third = factory.apply(VALUE);

        assertEquals(first, first);
        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(second, third);
        assertEquals(first, third);
        assertEquals(first.hashCode(), second.hashCode());

        Set<Identifier> identifiers = new HashSet<>();
        identifiers.add(first);
        identifiers.add(second);
        assertEquals(1, identifiers.size());
        assertTrue(identifiers.contains(third));
    }

    @ParameterizedTest
    @MethodSource("identifierFactories")
    @DisplayName("Given same type with different values, when compare, then not be equal")
    void givenSameTypeWithDifferentValues_whenCompare_thenNotBeEqual(Function<String, Identifier> factory) {
        Identifier first = factory.apply(VALUE);
        Identifier second = factory.apply(OTHER_VALUE);

        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    @ParameterizedTest
    @MethodSource("identifierFactories")
    @DisplayName("Given null or string with same value, when compare, then not be equal")
    void givenNullOrStringWithSameValue_whenCompare_thenNotBeEqual(Function<String, Identifier> factory) {
        Identifier identifier = factory.apply(VALUE);

        assertFalse(identifier.equals(null));
        assertFalse(identifier.equals(VALUE));
    }

    @Test
    @DisplayName("Given different types with same value, when compare, then keep identities separate")
    void givenDifferentTypesWithSameValue_whenCompare_thenKeepIdentitiesSeparate() {
        var identifiers = identifierFactories().map(factory -> factory.apply(VALUE)).toList();

        for (int i = 0; i < identifiers.size(); i++) {
            for (int j = i + 1; j < identifiers.size(); j++) {
                assertNotEquals(identifiers.get(i), identifiers.get(j));
                assertNotEquals(identifiers.get(j), identifiers.get(i));
            }
        }

        assertEquals(identifiers.size(), new HashSet<>(identifiers).size());
    }
}
