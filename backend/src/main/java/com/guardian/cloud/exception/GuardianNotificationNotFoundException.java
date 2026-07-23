package com.guardian.cloud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GuardianNotificationNotFoundException
        extends RuntimeException {

    public GuardianNotificationNotFoundException(
            Long notificationId
    ) {
        super(
                "Guardian notification not found: "
                        + notificationId
        );
    }
}