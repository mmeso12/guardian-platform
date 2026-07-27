package com.guardian.cloud.exception;

public class EmergencyEscalationNotFoundException
        extends RuntimeException {

    public EmergencyEscalationNotFoundException(
            Long escalationId
    ) {
        super(
                "Emergency escalation not found: "
                        + escalationId
        );
    }
}