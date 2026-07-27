package com.guardian.cloud.exception;

public class InvalidEmergencyEscalationStateException
        extends RuntimeException {

    public InvalidEmergencyEscalationStateException(
            String message
    ) {
        super(message);
    }
}