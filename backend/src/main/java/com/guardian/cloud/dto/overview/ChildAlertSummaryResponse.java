package com.guardian.cloud.dto.overview;

public record ChildAlertSummaryResponse(
        long openCount,
        boolean emergencyActive,
        ChildLatestAlertResponse latestAlert,
        ChildLatestAlertResponse latestOpenAlert
) {
}