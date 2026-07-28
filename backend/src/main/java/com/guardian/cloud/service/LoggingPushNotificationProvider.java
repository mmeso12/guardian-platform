package com.guardian.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LoggingPushNotificationProvider
        implements PushNotificationProvider {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    LoggingPushNotificationProvider.class
            );

    @Override
    public String providerName() {
        return "LOGGING";
    }

    @Override
    public PushDeliveryResult send(
            PushNotificationMessage message
    ) {
        if (
                message.pushToken() == null
                        || message.pushToken().isBlank()
        ) {
            return PushDeliveryResult.invalidToken(
                    "Push token is empty"
            );
        }

        /*
         * Useful values for testing failure handling without
         * Firebase:
         *
         * invalid-token-* → INVALID_TOKEN
         * fail-token-*    → retryable FAILED
         */
        if (
                message.pushToken()
                        .startsWith("invalid-token-")
        ) {
            return PushDeliveryResult.invalidToken(
                    "Development provider rejected token"
            );
        }

        if (
                message.pushToken()
                        .startsWith("fail-token-")
        ) {
            return PushDeliveryResult.failure(
                    "Simulated temporary provider failure"
            );
        }

        String providerMessageId =
                UUID.randomUUID().toString();

        LOGGER.info(
                """
                Push notification sent
                Platform: {}
                Token: {}
                Title: {}
                Body: {}
                Data: {}
                Provider message ID: {}
                """,
                message.platform(),
                maskToken(message.pushToken()),
                message.title(),
                message.body(),
                message.data(),
                providerMessageId
        );

        return PushDeliveryResult.success(
                providerMessageId
        );
    }

    private String maskToken(String token) {
        if (token.length() <= 8) {
            return "********";
        }

        return token.substring(0, 4)
                + "..."
                + token.substring(token.length() - 4);
    }
}