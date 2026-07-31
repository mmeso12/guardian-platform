package com.guardian.cloud.exception;

public class DeviceChildAssignmentNotFoundException
        extends RuntimeException {

    public DeviceChildAssignmentNotFoundException(
            Long deviceId
    ) {
        super(
                "No active child assignment found for device: "
                        + deviceId
        );
    }
}