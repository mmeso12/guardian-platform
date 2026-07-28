package com.guardian.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationRetryMonitor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    PushNotificationRetryMonitor.class
            );

    private final PushNotificationDispatcher
            dispatcher;

    public PushNotificationRetryMonitor(
            PushNotificationDispatcher dispatcher
    ) {
        this.dispatcher = dispatcher;
    }

    @Scheduled(
            fixedDelayString =
                    "${guardian.push-notification.retry-check-interval:30000}",
            initialDelayString =
                    "${guardian.push-notification.retry-initial-delay:30000}"
    )
    public void retryFailedDeliveries() {
        try {
            int processed =
                    dispatcher.retryDueDeliveries();

            if (processed > 0) {
                LOGGER.info(
                        "Processed {} push delivery retry attempt(s)",
                        processed
                );
            }
        } catch (Exception exception) {
            LOGGER.error(
                    "Push notification retry processing failed",
                    exception
            );
        }
    }
}