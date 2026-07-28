package com.guardian.cloud.entity;

public enum DeviceCommandStatus {

    PENDING,
    DELIVERED,
    RECEIVED,
    EXECUTING,

    COMPLETED,
    FAILED,
    CANCELLED,
    EXPIRED;

    public boolean isTerminal() {
        return this == COMPLETED
                || this == FAILED
                || this == CANCELLED
                || this == EXPIRED;
    }
}