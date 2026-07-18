package com.guardian.cloud.exception;

public class GuardianUserNotFoundException extends RuntimeException {

    public GuardianUserNotFoundException(String email) {
        super("Guardian user not found: " + email);
    }
}