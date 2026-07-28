package com.guardian.cloud.exception;

public class InvalidDeviceCommandStateException
        extends RuntimeException {

    public InvalidDeviceCommandStateException(
            Long commandId,
            String message
    ) {
        super(
                "Invalid state for device command "
                        + commandId
                        + ": "
                        + message
        );
    }
}