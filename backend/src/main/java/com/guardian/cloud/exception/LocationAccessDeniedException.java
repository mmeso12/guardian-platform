package com.guardian.cloud.exception;

public class LocationAccessDeniedException extends RuntimeException {

    public LocationAccessDeniedException(Long deviceId) {
        super(
                "You do not have permission to view the location of device "
                        + deviceId
        );
    }
}