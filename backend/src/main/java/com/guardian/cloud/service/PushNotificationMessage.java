package com.guardian.cloud.service;

import com.guardian.cloud.entity.MobilePlatform;

import java.util.Map;

public record PushNotificationMessage(
        String pushToken,
        MobilePlatform platform,
        String title,
        String body,
        Map<String, String> data
) {
}