package com.guardian.cloud.exception;

public class DeviceAlreadyPairedException extends RuntimeException {

    public DeviceAlreadyPairedException(String deviceUid) {
        super("Device is already paired: " + deviceUid);
    }
}