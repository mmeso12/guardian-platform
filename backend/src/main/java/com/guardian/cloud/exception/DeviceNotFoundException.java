package com.guardian.cloud.exception;

public class DeviceNotFoundException extends RuntimeException {

    public DeviceNotFoundException(String deviceUid) {
        super("Device not found: " + deviceUid);
    }
}