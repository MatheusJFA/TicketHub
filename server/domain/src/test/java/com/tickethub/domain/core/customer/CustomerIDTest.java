package com.tickethub.domain.core.customer;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.UUID;


@DisplayName("Customer ID")
public class CustomerIDTest {

    @Test
    @DisplayName("Given no value, when generate, then return valid uuid")
    void givenNoValue_whenGenerate_thenReturnValidUUID() {
        CustomerID id = CustomerID.generate();

        UUID uuid = UUID.fromString(id.getValue());

        assertEquals(uuid.toString(), id.getValue());
    }

    @Test
    @DisplayName("Given two generated i ds, when compare, then not be equal")
    void givenTwoGeneratedIDs_whenCompare_thenNotBeEqual() {
        CustomerID first = CustomerID.generate();
        CustomerID second = CustomerID.generate();

        assertNotEquals(first.getValue(), second.getValue());
        assertNotEquals(first, second);
    }

    @Test
    @DisplayName("Given a valid value, when from, then store value")
    void givenAValidValue_whenFrom_thenStoreValue() {
        String value = "cf4361d9-8e93-4c53-9ac5-629680d98469";

        CustomerID id = CustomerID.from(value);

        assertEquals(value, id.getValue());
    }

    @Test
    @DisplayName("Given a null value, when from, then throw null pointer exception")
    void givenANullValue_whenFrom_thenThrowNullPointerException() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> CustomerID.from(null)
        );

        assertEquals("'CustomerID' should not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Given the same instance, when compare, then be equal")
    void givenTheSameInstance_whenCompare_thenBeEqual() {
        CustomerID id = CustomerID.from("cf4361d9-8e93-4c53-9ac5-629680d98469");

        assertEquals(id, id);
    }

    @Test
    @DisplayName("Given i ds with same value, when compare, then be equal and have same hash code")
    void givenIDsWithSameValue_whenCompare_thenBeEqualAndHaveSameHashCode() {
        CustomerID first = CustomerID.from("cf4361d9-8e93-4c53-9ac5-629680d98469");
        CustomerID second = CustomerID.from("cf4361d9-8e93-4c53-9ac5-629680d98469");

        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    @DisplayName("Given i ds with different values, when compare, then not be equal")
    void givenIDsWithDifferentValues_whenCompare_thenNotBeEqual() {
        CustomerID first = CustomerID.from("cf4361d9-8e93-4c53-9ac5-629680d98469");
        CustomerID second = CustomerID.from("f2f50ac2-ef1e-42a5-89c1-b0527662b458");

        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    @Test
    @DisplayName("Given a null object, when compare, then not be equal")
    void givenANullObject_whenCompare_thenNotBeEqual() {
        CustomerID id = CustomerID.from("cf4361d9-8e93-4c53-9ac5-629680d98469");

        assertFalse(id.equals(null));
    }

    @Test
    @DisplayName("Given a string with same value, when compare, then not be equal")
    void givenAStringWithSameValue_whenCompare_thenNotBeEqual() {
        String value = "cf4361d9-8e93-4c53-9ac5-629680d98469";
        CustomerID id = CustomerID.from(value);

        assertFalse(id.equals(value));
    }
}
