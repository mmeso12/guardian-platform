package com.guardian.cloud.exception;

public class DuplicateDeviceEventException extends RuntimeException {

    public DuplicateDeviceEventException(
            String deviceUid,
            Long sequenceNumber
    ) {
        super(
                "Device event sequence " +
                sequenceNumber +
                " was already processed for device " +
                deviceUid
        );
    }
}