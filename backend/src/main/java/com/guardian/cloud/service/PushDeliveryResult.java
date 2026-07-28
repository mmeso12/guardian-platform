package com.guardian.cloud.service;

public record PushDeliveryResult(
        boolean successful,
        boolean invalidToken,
        String providerMessageId,
        String failureReason
) {

    public static PushDeliveryResult success(
            String providerMessageId
    ) {
        return new PushDeliveryResult(
                true,
                false,
                providerMessageId,
                null
        );
    }

    public static PushDeliveryResult failure(
            String failureReason
    ) {
        return new PushDeliveryResult(
                false,
                false,
                null,
                failureReason
        );
    }

    public static PushDeliveryResult invalidToken(
            String failureReason
    ) {
        return new PushDeliveryResult(
                false,
                true,
                null,
                failureReason
        );
    }
}