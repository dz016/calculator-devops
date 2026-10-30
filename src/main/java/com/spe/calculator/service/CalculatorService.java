package com.spe.calculator.service;

import com.spe.calculator.exception.DivisionByZeroException;
import org.springframework.stereotype.Service;

/**
 * Core arithmetic logic, kept free of any web concerns so it can be unit tested directly.
 */
@Service
public class CalculatorService {

    public double add(double a, double b) {
        requireFinite(a, b);
        return a + b;
    }

    public double subtract(double a, double b) {
        requireFinite(a, b);
        return a - b;
    }

    public double multiply(double a, double b) {
        requireFinite(a, b);
        return a * b;
    }

    public double divide(double a, double b) {
        requireFinite(a, b);
        if (b == 0) {
            throw new DivisionByZeroException();
        }
        return a / b;
    }

    // "NaN" and "Infinity" parse as valid doubles, so reject them explicitly.
    private static void requireFinite(double a, double b) {
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            throw new IllegalArgumentException("Parameters 'a' and 'b' must be finite numbers");
        }
    }
}
