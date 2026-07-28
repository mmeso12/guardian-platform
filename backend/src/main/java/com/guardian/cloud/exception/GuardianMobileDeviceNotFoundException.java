package com.guardian.cloud.exception;

public class GuardianMobileDeviceNotFoundException
        extends RuntimeException {

    public GuardianMobileDeviceNotFoundException(
            Long mobileDeviceId
    ) {
        super(
                "Guardian mobile device not found: "
                        + mobileDeviceId
        );
    }
}