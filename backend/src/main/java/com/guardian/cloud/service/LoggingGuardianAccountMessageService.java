package com.guardian.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingGuardianAccountMessageService
        implements GuardianAccountMessageService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    LoggingGuardianAccountMessageService.class
            );

    @Override
    public void sendEmailVerification(
            String email,
            String rawToken
    ) {
        LOGGER.info(
                "Email verification for {}: {}",
                email,
                rawToken
        );
    }

    @Override
    public void sendPasswordReset(
            String email,
            String rawToken
    ) {
        LOGGER.info(
                "Password reset for {}: {}",
                email,
                rawToken
        );
    }
}