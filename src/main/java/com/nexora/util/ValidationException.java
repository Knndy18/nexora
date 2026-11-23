package com.nexora.util;

/**
 * Excepción de regla de negocio o validación de campos.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
