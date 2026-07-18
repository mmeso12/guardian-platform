package com.guardian.cloud.exception;

public class DeviceIdentityMismatchException
        extends RuntimeException {

    public DeviceIdentityMismatchException(
            String authenticatedDeviceUid,
            String requestedDeviceUid
    ) {
        super(
                "Authenticated device " +
                authenticatedDeviceUid +
                " cannot submit data for " +
                requestedDeviceUid
        );
    }
}