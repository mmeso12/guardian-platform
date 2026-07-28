package com.guardian.cloud.exception;

public class DeviceCommandNotFoundException
        extends RuntimeException {

    public DeviceCommandNotFoundException(
            Long commandId
    ) {
        super(
                "Device command not found: "
                        + commandId
        );
    }
}