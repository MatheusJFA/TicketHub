package com.tickethub.domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tickethub.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Text")
public class TextTest {

    @ParameterizedTest
    @ValueSource(strings = {"A", "123", "Show 2026: ingresso #42!", "Descrição — ação, música & diversão."})
    @DisplayName("Given a valid text, when create, then store value")
    void givenAValidText_whenCreate_thenStoreValue(String value) {
        assertEquals(value, Text.create(value).getValue());
    }

    @Test
    @DisplayName("Given a text with whitespace, when create, then preserve formatting")
    void givenATextWithWhitespace_whenCreate_thenPreserveFormatting() {
        String value = "  Primeira  linha\n\tSegunda linha\r\n  ";

        assertEquals(value, Text.create(value).getValue());
    }

    @Test
    @DisplayName("Given a text at max length, when create, then store entire value")
    void givenATextAtMaxLength_whenCreate_thenStoreEntireValue() {
        String value = "A".repeat(1000);

        assertEquals(value, Text.create(value).getValue());
    }

    @Test
    @DisplayName("Given a text over max length, when create, then throw domain exception")
    void givenATextOverMaxLength_whenCreate_thenThrowDomainException() {
        String value = "A".repeat(1001);

        DomainException exception = assertThrows(DomainException.class, () -> Text.create(value));

        assertEquals("Invalid text " + value, exception.getMessage());
    }

    @Test
    @DisplayName("Given a blank text over max length, when create, then store value")
    void givenABlankTextOverMaxLength_whenCreate_thenStoreValue() {
        String value = " ".repeat(1001);

        assertEquals(value, Text.create(value).getValue());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n\r", " \t\n ", "\u2003"})
    @DisplayName("Given a null or blank text, when create, then store value")
    void givenANullOrBlankText_whenCreate_thenStoreValue(String value) {
        assertEquals(value, Text.create(value).getValue());
    }

    @Test
    @DisplayName("Given a text, when to string, then return original value")
    void givenAText_whenToString_thenReturnOriginalValue() {
        String value = "  Observação: levar documento.\nEntrada às 20h.  ";

        assertEquals(value, Text.create(value).toString());
    }

    @Test
    @DisplayName("Given texts with same value, when compare, then be equal and have same hash code")
    void givenTextsWithSameValue_whenCompare_thenBeEqualAndHaveSameHashCode() {
        Text first = Text.create("Descrição do evento");
        Text second = Text.create("Descrição do evento");

        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Outro evento", "evento", " Evento", "Evento "})
    @DisplayName("Given texts with different values, when compare, then not be equal")
    void givenTextsWithDifferentValues_whenCompare_thenNotBeEqual(String value) {
        assertNotEquals(Text.create("Evento"), Text.create(value));
    }

    @Test
    @DisplayName("Given a null or different type, when compare, then not be equal")
    void givenANullOrDifferentType_whenCompare_thenNotBeEqual() {
        Text text = Text.create("Evento");

        assertFalse(text.equals(null));
        assertFalse(text.equals(text.getValue()));
        assertFalse(text.equals(Name.create("Evento")));
    }
}
