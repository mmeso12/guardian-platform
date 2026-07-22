package com.guardian.cloud.exception;

public class InvalidAlertStateException
        extends RuntimeException {

    public InvalidAlertStateException(String message) {
        super(message);
    }
}