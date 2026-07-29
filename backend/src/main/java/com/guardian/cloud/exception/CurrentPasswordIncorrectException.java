package com.guardian.cloud.exception;

public class CurrentPasswordIncorrectException
        extends RuntimeException {

    public CurrentPasswordIncorrectException() {
        super("Current password is incorrect");
    }
}