package com.tickethub.domain.shared;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tickethub.domain.exception.DomainException;

public class MoneyTest {

    @Test
    void givenNullValue_whenCreate_thenThrowDomainException() {
        assertEquals("Invalid money", assertThrows(DomainException.class,
                () -> Money.create(null, Currency.getInstance("BRL"))).getMessage());
    }

    @Test
    void givenNullCurrency_whenCreate_thenThrowDomainException() {
        assertEquals("Invalid money", assertThrows(DomainException.class,
                () -> Money.create(BigDecimal.ONE, null)).getMessage());
    }
    private static final Currency BRL = Currency.getInstance("BRL");

    @ParameterizedTest
    @CsvSource({
            "0, 0.00",
            "0.01, 0.01",
            "10, 10.00",
            "10.1, 10.10",
            "10.12, 10.12",
            "10.1200, 10.12",
            "0.0000, 0.00",
            "1E+3, 1000.00",
            "12345678901234567890.12, 12345678901234567890.12"
    })
    void givenAValidAmount_whenCreate_thenStoreValueWithTwoDecimalPlaces(String input, String expected) {
        Money money = Money.create(new BigDecimal(input), BRL);

        assertEquals(new BigDecimal(expected), money.getValue());
    }

    @Test
    void givenANullAmount_whenCreate_thenThrowDomainException() {
        DomainException exception = assertThrows(DomainException.class, () -> Money.create(null, BRL));

        assertEquals("Invalid money", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "-1", "-100.50"})
    void givenANegativeAmount_whenCreate_thenThrowDomainException(String value) {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> Money.create(new BigDecimal(value), BRL)
        );

        assertEquals("Invalid money", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.001", "10.123", "10.999", "1E-3"})
    void givenAnAmountWithFractionalCents_whenCreate_thenThrowDomainException(String value) {
        assertThrows(DomainException.class, () -> Money.create(new BigDecimal(value), BRL));
    }

    @Test
    void givenAnExactDecimalSum_whenCreate_thenPreservePrecision() {
        BigDecimal amount = new BigDecimal("0.10").add(new BigDecimal("0.20"));

        assertEquals(new BigDecimal("0.30"), Money.create(amount, BRL).getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"10", "10.0", "10.00", "10.0000", "1E+1"})
    void givenSameAmountsWithDifferentScales_whenCompare_thenBeEqualAndHaveSameHashCode(String value) {
        Money first = Money.create(new BigDecimal("10.00"), BRL);
        Money second = Money.create(new BigDecimal(value), BRL);

        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void givenDifferentAmounts_whenCompare_thenNotBeEqual() {
        assertNotEquals(Money.create(new BigDecimal("10.00"), BRL), Money.create(new BigDecimal("10.01"), BRL));
    }

    @Test
    void givenEquivalentAmounts_whenUseInSet_thenFindSameValue() {
        Set<Money> amounts = new HashSet<>();
        amounts.add(Money.create(new BigDecimal("10.0"), BRL));
        amounts.add(Money.create(new BigDecimal("10.00"), BRL));

        assertEquals(1, amounts.size());
        assertTrue(amounts.contains(Money.create(new BigDecimal("10"), BRL)));
    }

    @Test
    void givenANullOrDifferentType_whenCompare_thenNotBeEqual() {
        Money money = Money.create(new BigDecimal("10.00"), BRL);

        assertFalse(money.equals(null));
        assertFalse(money.equals(money.getValue()));
        assertFalse(money.equals("10.00"));
    }

    @ParameterizedTest
    @CsvSource({"0, 0.00", "10.1, 10.10", "1E+3, 1000.00"})
    void givenAnAmount_whenToString_thenIncludeCurrencyAndTwoDecimalPlaces(String value, String expected) {
        assertEquals("BRL " + expected, Money.create(new BigDecimal(value), BRL).toString());
    }

    @ParameterizedTest
    @CsvSource({
            "BRL, 10, 10.00",
            "USD, 10.1, 10.10",
            "EUR, 0.01, 0.01",
            "JPY, 100, 100",
            "JPY, 100.00, 100",
            "JPY, 0.000, 0",
            "KWD, 10.123, 10.123",
            "KWD, 10.1, 10.100",
            "CLF, 10.1234, 10.1234"
    })
    void givenAnAmountAndCurrency_whenCreate_thenUseCurrencyPrecision(String code, String input, String expected) {
        Currency currency = Currency.getInstance(code);

        Money money = Money.create(new BigDecimal(input), currency);

        assertEquals(currency, money.getCurrency());
        assertEquals(new BigDecimal(expected), money.getValue());
        assertEquals(code + " " + expected, money.toString());
    }

    @Test
    void givenANullCurrency_whenCreate_thenThrowDomainException() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> Money.create(BigDecimal.TEN, null)
        );

        assertEquals("Invalid money", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"XXX", "XDR", "XAU"})
    void givenACurrencyWithoutDefinedPrecision_whenCreate_thenThrowDomainException(String code) {
        Currency currency = Currency.getInstance(code);

        assertThrows(DomainException.class, () -> Money.create(BigDecimal.TEN, currency));
    }

    @ParameterizedTest
    @CsvSource({"JPY, 0.01", "USD, 10.123", "KWD, 10.1234", "CLF, 10.12345"})
    void givenAnAmountExceedingCurrencyPrecision_whenCreate_thenThrowDomainException(String code, String value) {
        Currency currency = Currency.getInstance(code);

        assertThrows(DomainException.class, () -> Money.create(new BigDecimal(value), currency));
    }

    @ParameterizedTest
    @ValueSource(strings = {"USD", "EUR", "JPY"})
    void givenSameAmountInDifferentCurrencies_whenCompare_thenNotBeEqual(String code) {
        Money first = Money.create(BigDecimal.TEN, BRL);
        Money second = Money.create(BigDecimal.TEN, Currency.getInstance(code));

        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    @Test
    void givenSameAmountInDifferentCurrencies_whenUseInSet_thenKeepBoth() {
        Set<Money> amounts = new HashSet<>();
        amounts.add(Money.create(BigDecimal.TEN, BRL));
        amounts.add(Money.create(BigDecimal.TEN, Currency.getInstance("USD")));

        assertEquals(2, amounts.size());
        assertTrue(amounts.contains(Money.create(new BigDecimal("10.00"), BRL)));
        assertTrue(amounts.contains(Money.create(new BigDecimal("10.0"), Currency.getInstance("USD"))));
    }
}
