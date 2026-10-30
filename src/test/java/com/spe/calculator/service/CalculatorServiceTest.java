package com.spe.calculator.service;

import com.spe.calculator.exception.DivisionByZeroException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Plain JUnit 5 unit tests: no Spring context, just the arithmetic.
 */
class CalculatorServiceTest {

    private final CalculatorService service = new CalculatorService();

    @Test
    void addReturnsSum() {
        assertEquals(5, service.add(2, 3));
        assertEquals(-1.5, service.add(-3, 1.5));
    }

    @Test
    void subtractReturnsDifference() {
        assertEquals(-1, service.subtract(2, 3));
        assertEquals(10, service.subtract(5, -5));
    }

    @Test
    void multiplyReturnsProduct() {
        assertEquals(6, service.multiply(2, 3));
        assertEquals(0, service.multiply(0, 99));
    }

    @Test
    void divideReturnsQuotient() {
        assertEquals(2.5, service.divide(5, 2));
        assertEquals(-4, service.divide(8, -2));
    }

    @Test
    void divideByZeroThrows() {
        assertThrows(DivisionByZeroException.class, () -> service.divide(1, 0));
    }

    @Test
    void nonFiniteInputThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.add(Double.NaN, 1));
        assertThrows(IllegalArgumentException.class, () -> service.multiply(Double.POSITIVE_INFINITY, 2));
    }
}
