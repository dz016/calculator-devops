package com.spe.calculator.dto;

/**
 * Success body, e.g. { "operation": "add", "a": 2, "b": 3, "result": 5 }.
 */
public record CalculationResponse(String operation, double a, double b, double result) {
}
