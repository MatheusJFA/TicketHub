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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tickethub.domain.exception.DomainException;

@DisplayName("Money")
public class MoneyTest {

    @Test
    @DisplayName("Given null value, when create, then throws DomainException")
    void givenNullValue_whenCreate_thenThrowDomainException() {
        final var exception = assertThrows(DomainException.class,
                () -> Money.create(null, Currency.getInstance("BRL")),
                () -> "Creating Money with null value should throw DomainException");

        assertEquals("Invalid money", exception.getMessage(),
                () -> "Exception message should be \"Invalid money\" for null value");
    }

    @Test
    @DisplayName("Given null currency, when create, then throws DomainException")
    void givenNullCurrency_whenCreate_thenThrowDomainException() {
        final var exception = assertThrows(DomainException.class,
                () -> Money.create(BigDecimal.ONE, null),
                () -> "Creating Money with null currency should throw DomainException");

        assertEquals("Invalid money", exception.getMessage(),
                () -> "Exception message should be \"Invalid money\" for null currency");
    }

    private static final Currency BRL = Currency.getInstance("BRL");

    @ParameterizedTest(name = "Given valid amount \"{0}\", when create, then stores value with two decimal places \"{1}\"")
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
    @DisplayName("Given valid amount, when create, then stores value with two decimal places")
    void givenAValidAmount_whenCreate_thenStoreValueWithTwoDecimalPlaces(String input, String expected) {
        Money money = Money.create(new BigDecimal(input), BRL);

        assertEquals(new BigDecimal(expected), money.getValue(),
                () -> "Money value for input [" + input + "] should be [" + expected + "]");
    }

    @Test
    @DisplayName("Given null amount, when create, then throws DomainException")
    void givenANullAmount_whenCreate_thenThrowDomainException() {
        final var exception = assertThrows(DomainException.class, () -> Money.create(null, BRL),
                () -> "Creating Money with null amount should throw DomainException");

        assertEquals("Invalid money", exception.getMessage(),
                () -> "Exception message should be \"Invalid money\" for null amount");
    }

    @ParameterizedTest(name = "Given negative amount \"{0}\", when create, then throws DomainException")
    @ValueSource(strings = {"-0.01", "-1", "-100.50"})
    @DisplayName("Given negative amount, when create, then throws DomainException")
    void givenANegativeAmount_whenCreate_thenThrowDomainException(String value) {
        final var exception = assertThrows(
                DomainException.class,
                () -> Money.create(new BigDecimal(value), BRL),
                () -> "Creating Money with negative amount [" + value + "] should throw DomainException"
        );

        assertEquals("Invalid money", exception.getMessage(),
                () -> "Exception message should be \"Invalid money\" for value [" + value + "]");
    }

    @ParameterizedTest(name = "Given amount with fractional cents \"{0}\", when create, then throws DomainException")
    @ValueSource(strings = {"0.001", "10.123", "10.999", "1E-3"})
    @DisplayName("Given amount with fractional cents, when create, then throws DomainException")
    void givenAnAmountWithFractionalCents_whenCreate_thenThrowDomainException(String value) {
        final var exception = assertThrows(DomainException.class, () -> Money.create(new BigDecimal(value), BRL),
                () -> "Creating Money with fractional cents [" + value + "] should throw DomainException");

        assertEquals("Invalid money", exception.getMessage(),
                () -> "Exception message should be \"Invalid money\" for value [" + value + "]");
    }

    @Test
    @DisplayName("Given exact decimal sum, when create, then preserves precision")
    void givenAnExactDecimalSum_whenCreate_thenPreservePrecision() {
        BigDecimal amount = new BigDecimal("0.10").add(new BigDecimal("0.20"));

        assertEquals(new BigDecimal("0.30"), Money.create(amount, BRL).getValue(),
                () -> "Money should preserve precision for exact decimal sum");
    }

    @ParameterizedTest(name = "Given same amount with different scale \"{0}\", when compare, then are equal with same hashCode")
    @ValueSource(strings = {"10", "10.0", "10.00", "10.0000", "1E+1"})
    @DisplayName("Given same amounts with different scales, when compare, then are equal with same hashCode")
    void givenSameAmountsWithDifferentScales_whenCompare_thenBeEqualAndHaveSameHashCode(String value) {
        Money first = Money.create(new BigDecimal("10.00"), BRL);
        Money second = Money.create(new BigDecimal(value), BRL);

        assertEquals(first, second,
                () -> "Money amounts [10.00] and [" + value + "] should be equal");
        assertEquals(second, first,
                () -> "Money equality should be symmetric for [" + value + "]");
        assertEquals(first.hashCode(), second.hashCode(),
                () -> "Equal Money amounts should have the same hashCode for [" + value + "]");
    }

    @Test
    @DisplayName("Given different amounts, when compare, then are not equal")
    void givenDifferentAmounts_whenCompare_thenNotBeEqual() {
        assertNotEquals(Money.create(new BigDecimal("10.00"), BRL), Money.create(new BigDecimal("10.01"), BRL),
                () -> "Different Money amounts should not be equal");
    }

    @Test
    @DisplayName("Given equivalent amounts, when use in set, then find same value")
    void givenEquivalentAmounts_whenUseInSet_thenFindSameValue() {
        Set<Money> amounts = new HashSet<>();
        amounts.add(Money.create(new BigDecimal("10.0"), BRL));
        amounts.add(Money.create(new BigDecimal("10.00"), BRL));

        assertEquals(1, amounts.size(),
                () -> "Equivalent Money amounts should collapse to a single set entry");
        assertTrue(amounts.contains(Money.create(new BigDecimal("10"), BRL)),
                () -> "Set should contain Money with value 10");
    }

    @Test
    @DisplayName("Given null or different type, when compare, then are not equal")
    void givenANullOrDifferentType_whenCompare_thenNotBeEqual() {
        Money money = Money.create(new BigDecimal("10.00"), BRL);

        assertFalse(money.equals(null),
                () -> "Money should not be equal to null");
        assertFalse(money.equals(money.getValue()),
                () -> "Money should not be equal to a BigDecimal");
        assertFalse(money.equals("10.00"),
                () -> "Money should not be equal to a String");
    }

    @ParameterizedTest(name = "Given amount \"{0}\", when toString, then includes currency and two decimal places \"{1}\"")
    @CsvSource({"0, 0.00", "10.1, 10.10", "1E+3, 1000.00"})
    @DisplayName("Given amount, when toString, then includes currency and two decimal places")
    void givenAnAmount_whenToString_thenIncludeCurrencyAndTwoDecimalPlaces(String value, String expected) {
        assertEquals("BRL " + expected, Money.create(new BigDecimal(value), BRL).toString(),
                () -> "Money toString for [" + value + "] should be [BRL " + expected + "]");
    }

    @ParameterizedTest(name = "Given amount \"{1}\" and currency \"{0}\", when create, then uses currency precision \"{2}\"")
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
    @DisplayName("Given amount and currency, when create, then uses currency precision")
    void givenAnAmountAndCurrency_whenCreate_thenUseCurrencyPrecision(String code, String input, String expected) {
        Currency currency = Currency.getInstance(code);

        Money money = Money.create(new BigDecimal(input), currency);

        assertEquals(currency, money.getCurrency(),
                () -> "Money currency should be [" + code + "]");
        assertEquals(new BigDecimal(expected), money.getValue(),
                () -> "Money value for [" + input + " " + code + "] should be [" + expected + "]");
        assertEquals(code + " " + expected, money.toString(),
                () -> "Money toString should be [" + code + " " + expected + "]");
    }

    @Test
    @DisplayName("Given null currency with amount, when create, then throws DomainException")
    void givenANullCurrency_whenCreate_thenThrowDomainException() {
        final var exception = assertThrows(
                DomainException.class,
                () -> Money.create(BigDecimal.TEN, null),
                () -> "Creating Money with null currency should throw DomainException"
        );

        assertEquals("Invalid money", exception.getMessage(),
                () -> "Exception message should be \"Invalid money\" for null currency");
    }

    @ParameterizedTest(name = "Given currency without defined precision \"{0}\", when create, then throws DomainException")
    @ValueSource(strings = {"XXX", "XDR", "XAU"})
    @DisplayName("Given currency without defined precision, when create, then throws DomainException")
    void givenACurrencyWithoutDefinedPrecision_whenCreate_thenThrowDomainException(String code) {
        Currency currency = Currency.getInstance(code);

        final var exception = assertThrows(DomainException.class, () -> Money.create(BigDecimal.TEN, currency),
                () -> "Creating Money with currency [" + code + "] without defined precision should throw DomainException");

        assertEquals("Invalid money", exception.getMessage(),
                () -> "Exception message should be \"Invalid money\" for currency [" + code + "]");
    }

    @ParameterizedTest(name = "Given amount \"{1}\" exceeding precision of \"{0}\", when create, then throws DomainException")
    @CsvSource({"JPY, 0.01", "USD, 10.123", "KWD, 10.1234", "CLF, 10.12345"})
    @DisplayName("Given amount exceeding currency precision, when create, then throws DomainException")
    void givenAnAmountExceedingCurrencyPrecision_whenCreate_thenThrowDomainException(String code, String value) {
        Currency currency = Currency.getInstance(code);

        final var exception = assertThrows(DomainException.class, () -> Money.create(new BigDecimal(value), currency),
                () -> "Creating Money with value [" + value + "] exceeding precision of [" + code + "] should throw DomainException");

        assertEquals("Invalid money", exception.getMessage(),
                () -> "Exception message should be \"Invalid money\" for [" + value + " " + code + "]");
    }

    @ParameterizedTest(name = "Given same amount in different currency \"{0}\", when compare, then are not equal")
    @ValueSource(strings = {"USD", "EUR", "JPY"})
    @DisplayName("Given same amount in different currencies, when compare, then are not equal")
    void givenSameAmountInDifferentCurrencies_whenCompare_thenNotBeEqual(String code) {
        Money first = Money.create(BigDecimal.TEN, BRL);
        Money second = Money.create(BigDecimal.TEN, Currency.getInstance(code));

        assertNotEquals(first, second,
                () -> "Same amount in BRL and [" + code + "] should not be equal");
        assertNotEquals(second, first,
                () -> "Money inequality should be symmetric for [" + code + "]");
    }

    @Test
    @DisplayName("Given same amount in different currencies, when use in set, then keep both")
    void givenSameAmountInDifferentCurrencies_whenUseInSet_thenKeepBoth() {
        Set<Money> amounts = new HashSet<>();
        amounts.add(Money.create(BigDecimal.TEN, BRL));
        amounts.add(Money.create(BigDecimal.TEN, Currency.getInstance("USD")));

        assertEquals(2, amounts.size(),
                () -> "Money in different currencies should be kept as distinct set entries");
        assertTrue(amounts.contains(Money.create(new BigDecimal("10.00"), BRL)),
                () -> "Set should contain BRL 10.00");
        assertTrue(amounts.contains(Money.create(new BigDecimal("10.0"), Currency.getInstance("USD"))),
                () -> "Set should contain USD 10.0");
    }

    @Test
    @DisplayName("Given two amounts, when add, then return summed amount")
    void givenTwoAmounts_whenAdd_thenReturnSummedAmount() {
        Money first = Money.create(new BigDecimal("50.00"), BRL);
        Money second = Money.create(new BigDecimal("25.50"), BRL);

        assertEquals(Money.create(new BigDecimal("75.50"), BRL), first.add(second),
                () -> "Adding BRL 50.00 and BRL 25.50 should return BRL 75.50");
    }

    @Test
    @DisplayName("Given different currencies, when add, then throw DomainException")
    void givenDifferentCurrencies_whenAdd_thenThrowDomainException() {
        Money first = Money.create(BigDecimal.TEN, BRL);
        Money second = Money.create(BigDecimal.TEN, Currency.getInstance("USD"));

        final var exception = assertThrows(DomainException.class,
                () -> first.add(second),
                () -> "Adding amounts in different currencies should throw DomainException");

        assertEquals("Cannot add money with different currencies", exception.getMessage(),
                () -> "Exception message should indicate the currency mismatch");
    }
}
