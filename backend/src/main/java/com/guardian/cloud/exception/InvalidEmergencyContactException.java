package com.guardian.cloud.exception;

public class InvalidEmergencyContactException
        extends RuntimeException {

    public InvalidEmergencyContactException(
            String message
    ) {
        super(message);
    }
}