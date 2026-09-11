package com.tickethub.domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tickethub.domain.exception.DomainException;

public class TextTest {

    @ParameterizedTest
    @ValueSource(strings = {"A", "123", "Show 2026: ingresso #42!", "Descrição — ação, música & diversão."})
    void givenAValidText_whenCreate_thenStoreValue(String value) {
        assertEquals(value, Text.create(value).getValue());
    }

    @Test
    void givenATextWithWhitespace_whenCreate_thenPreserveFormatting() {
        String value = "  Primeira  linha\n\tSegunda linha\r\n  ";

        assertEquals(value, Text.create(value).getValue());
    }

    @Test
    void givenATextAtMaxLength_whenCreate_thenStoreEntireValue() {
        String value = "A".repeat(1000);

        assertEquals(value, Text.create(value).getValue());
    }

    @Test
    void givenATextOverMaxLength_whenCreate_thenThrowDomainException() {
        String value = "A".repeat(1001);

        DomainException exception = assertThrows(DomainException.class, () -> Text.create(value));

        assertEquals("Invalid text " + value, exception.getMessage());
    }

    @Test
    void givenABlankTextOverMaxLength_whenCreate_thenStoreValue() {
        String value = " ".repeat(1001);

        assertEquals(value, Text.create(value).getValue());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n\r", " \t\n ", "\u2003"})
    void givenANullOrBlankText_whenCreate_thenStoreValue(String value) {
        assertEquals(value, Text.create(value).getValue());
    }

    @Test
    void givenAText_whenToString_thenReturnOriginalValue() {
        String value = "  Observação: levar documento.\nEntrada às 20h.  ";

        assertEquals(value, Text.create(value).toString());
    }

    @Test
    void givenTextsWithSameValue_whenCompare_thenBeEqualAndHaveSameHashCode() {
        Text first = Text.create("Descrição do evento");
        Text second = Text.create("Descrição do evento");

        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Outro evento", "evento", " Evento", "Evento "})
    void givenTextsWithDifferentValues_whenCompare_thenNotBeEqual(String value) {
        assertNotEquals(Text.create("Evento"), Text.create(value));
    }

    @Test
    void givenANullOrDifferentType_whenCompare_thenNotBeEqual() {
        Text text = Text.create("Evento");

        assertFalse(text.equals(null));
        assertFalse(text.equals(text.getValue()));
        assertFalse(text.equals(Name.create("Evento")));
    }
}
