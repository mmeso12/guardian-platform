package com.guardian.cloud.exception;

public class DeviceAlreadyAssignedException
        extends RuntimeException {

    public DeviceAlreadyAssignedException(
            Long deviceId
    ) {
        super(
                "Device is already assigned to an active child profile: "
                        + deviceId
        );
    }
}