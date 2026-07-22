package com.guardian.cloud.exception;

public class GeofenceAccessDeniedException
        extends RuntimeException {

    public GeofenceAccessDeniedException(Long deviceId) {
        super(
                "You do not have permission to manage geofences "
                        + "for device " + deviceId
        );
    }
}