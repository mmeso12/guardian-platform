package com.guardian.cloud.exception;

public class PushTokenAlreadyRegisteredException
        extends RuntimeException {

    public PushTokenAlreadyRegisteredException() {
        super(
                "Push token is already registered to another guardian"
        );
    }
}