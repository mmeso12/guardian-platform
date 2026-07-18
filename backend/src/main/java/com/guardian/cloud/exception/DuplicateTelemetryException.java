package com.guardian.cloud.exception;

public class DuplicateTelemetryException extends RuntimeException {

    public DuplicateTelemetryException(
            String deviceUid,
            Long sequenceNumber
    ) {
        super(
                "Telemetry sequence " + sequenceNumber +
                " was already processed for device " + deviceUid
        );
    }
}