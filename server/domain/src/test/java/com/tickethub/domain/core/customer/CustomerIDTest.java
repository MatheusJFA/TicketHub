package com.tickethub.domain.core.customer;


import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;


public class CustomerIDTest {

    @Test
    void givenNoValue_whenGenerate_thenReturnValidUUID() {
        CustomerID id = CustomerID.generate();

        UUID uuid = UUID.fromString(id.getValue());

        assertEquals(uuid.toString(), id.getValue());
    }

    @Test
    void givenTwoGeneratedIDs_whenCompare_thenNotBeEqual() {
        CustomerID first = CustomerID.generate();
        CustomerID second = CustomerID.generate();

        assertNotEquals(first.getValue(), second.getValue());
        assertNotEquals(first, second);
    }

    @Test
    void givenAValidValue_whenFrom_thenStoreValue() {
        String value = "cf4361d9-8e93-4c53-9ac5-629680d98469";

        CustomerID id = CustomerID.from(value);

        assertEquals(value, id.getValue());
    }

    @Test
    void givenANullValue_whenFrom_thenThrowNullPointerException() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> CustomerID.from(null)
        );

        assertEquals("'CustomerID' should not be null", exception.getMessage());
    }

    @Test
    void givenTheSameInstance_whenCompare_thenBeEqual() {
        CustomerID id = CustomerID.from("cf4361d9-8e93-4c53-9ac5-629680d98469");

        assertEquals(id, id);
    }

    @Test
    void givenIDsWithSameValue_whenCompare_thenBeEqualAndHaveSameHashCode() {
        CustomerID first = CustomerID.from("cf4361d9-8e93-4c53-9ac5-629680d98469");
        CustomerID second = CustomerID.from("cf4361d9-8e93-4c53-9ac5-629680d98469");

        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void givenIDsWithDifferentValues_whenCompare_thenNotBeEqual() {
        CustomerID first = CustomerID.from("cf4361d9-8e93-4c53-9ac5-629680d98469");
        CustomerID second = CustomerID.from("f2f50ac2-ef1e-42a5-89c1-b0527662b458");

        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    @Test
    void givenANullObject_whenCompare_thenNotBeEqual() {
        CustomerID id = CustomerID.from("cf4361d9-8e93-4c53-9ac5-629680d98469");

        assertFalse(id.equals(null));
    }

    @Test
    void givenAStringWithSameValue_whenCompare_thenNotBeEqual() {
        String value = "cf4361d9-8e93-4c53-9ac5-629680d98469";
        CustomerID id = CustomerID.from(value);

        assertFalse(id.equals(value));
    }
}
