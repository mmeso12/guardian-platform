package com.guardian.cloud.exception;

public class AlertAccessDeniedException
        extends RuntimeException {

    public AlertAccessDeniedException() {
        super("You do not have permission to access this alert");
    }
}