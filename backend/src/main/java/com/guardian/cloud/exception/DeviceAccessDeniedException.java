package com.guardian.cloud.exception;

public class DeviceAccessDeniedException extends RuntimeException {

    public DeviceAccessDeniedException(Long deviceId) {
        super("You do not have permission to access device " + deviceId);
    }
}