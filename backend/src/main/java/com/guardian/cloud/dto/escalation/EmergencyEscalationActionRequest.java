package com.guardian.cloud.dto.escalation;

import jakarta.validation.constraints.Size;

public record EmergencyEscalationActionRequest(

        @Size(max = 1000)
        String note
) {
}