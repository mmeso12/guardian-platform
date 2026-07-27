package com.guardian.cloud.exception;

public class EmergencyContactNotFoundException
        extends RuntimeException {

    public EmergencyContactNotFoundException(
            Long contactId
    ) {
        super(
                "Emergency contact not found: "
                        + contactId
        );
    }
}