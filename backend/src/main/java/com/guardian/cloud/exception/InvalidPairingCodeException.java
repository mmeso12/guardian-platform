package com.guardian.cloud.exception;

public class InvalidPairingCodeException extends RuntimeException {

    public InvalidPairingCodeException() {
        super("Invalid device UID or pairing code");
    }
}