package com.guardian.cloud.exception;

public class DuplicateEmergencyContactPriorityException
        extends RuntimeException {

    public DuplicateEmergencyContactPriorityException(
            Integer priority
    ) {
        super(
                "Another emergency contact already uses priority "
                        + priority
        );
    }
}