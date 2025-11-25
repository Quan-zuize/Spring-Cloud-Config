package com.quanna.domain.exception;

/**
 * Exception thrown when an operation is not authorized
 */
public class UnauthorizedException extends DomainException {

    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED");
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, "UNAUTHORIZED", cause);
    }
}

