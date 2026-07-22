package com.guardian.cloud.dto.alert;

public record AlertUserResponse(
        Long id,
        String firstName,
        String lastName,
        String email
) {
}