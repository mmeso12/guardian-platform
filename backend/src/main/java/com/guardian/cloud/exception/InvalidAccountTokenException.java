package com.guardian.cloud.exception;

public class InvalidAccountTokenException
        extends RuntimeException {

    public InvalidAccountTokenException() {
        super("Account token is invalid or expired");
    }
}