package com.guardian.cloud.service;

public interface GuardianAccountMessageService {

    void sendEmailVerification(
            String email,
            String rawToken
    );

    void sendPasswordReset(
            String email,
            String rawToken
    );
}