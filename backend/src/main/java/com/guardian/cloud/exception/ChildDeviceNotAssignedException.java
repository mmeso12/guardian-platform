package com.guardian.cloud.exception;

import java.util.UUID;

public class ChildDeviceNotAssignedException
        extends RuntimeException {

    public ChildDeviceNotAssignedException(
            UUID childId
    ) {
        super(
                "No active device is assigned to child: "
                        + childId
        );
    }
}