package com.guardian.cloud.service;

public interface PushNotificationProvider {

    String providerName();

    PushDeliveryResult send(
            PushNotificationMessage message
    );
}