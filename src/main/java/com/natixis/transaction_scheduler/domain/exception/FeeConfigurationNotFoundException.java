package com.natixis.transaction_scheduler.domain.exception;

public class FeeConfigurationNotFoundException extends RuntimeException {

    public FeeConfigurationNotFoundException(String message) {
        super(message);
    }

    public FeeConfigurationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
