package com.guardian.cloud.dto.alert;

import jakarta.validation.constraints.Size;

public record AlertActionRequest(

        @Size(max = 1000)
        String note
) {
}