package com.guardian.cloud.exception;

public class GuardianAlertNotFoundException
        extends RuntimeException {

    public GuardianAlertNotFoundException(Long alertId) {
        super("Guardian alert not found: " + alertId);
    }
}