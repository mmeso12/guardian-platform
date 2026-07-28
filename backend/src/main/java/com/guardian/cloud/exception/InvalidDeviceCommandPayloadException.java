package com.guardian.cloud.exception;

public class InvalidDeviceCommandPayloadException
        extends RuntimeException {

    public InvalidDeviceCommandPayloadException(
            String message
    ) {
        super(message);
    }
}