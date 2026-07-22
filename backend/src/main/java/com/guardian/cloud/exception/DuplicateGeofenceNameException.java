package com.guardian.cloud.exception;

public class DuplicateGeofenceNameException
        extends RuntimeException {

    public DuplicateGeofenceNameException(String name) {
        super(
                "A geofence with the name '"
                        + name
                        + "' already exists for this device"
        );
    }
}