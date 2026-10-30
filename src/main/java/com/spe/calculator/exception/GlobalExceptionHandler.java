package com.spe.calculator.exception;

import com.spe.calculator.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Turns every client mistake into HTTP 400 with the same JSON error shape.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DivisionByZeroException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDivisionByZero(DivisionByZeroException ex, HttpServletRequest request) {
        return ErrorResponse.badRequest(ex.getMessage(), request.getRequestURI());
    }

    // e.g. /api/v1/add?a=2 (no b)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return ErrorResponse.badRequest("Missing required parameter '" + ex.getParameterName() + "'", request.getRequestURI());
    }

    // e.g. /api/v1/add?a=abc&b=1
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return ErrorResponse.badRequest("Parameter '" + ex.getName() + "' must be a number", request.getRequestURI());
    }

    // NaN / Infinity rejected by CalculatorService
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return ErrorResponse.badRequest(ex.getMessage(), request.getRequestURI());
    }
}
