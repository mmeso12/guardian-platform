package com.guardian.cloud.dto.overview;

import java.time.Instant;

public record ChildSafetyOverviewResponse(
        ChildOverviewProfileResponse child,
        boolean deviceAssigned,
        ChildDeviceOverviewResponse device,
        ChildLatestLocationResponse latestLocation,
        ChildAlertSummaryResponse alerts,
        ChildGeofenceSummaryResponse geofences,
        Instant generatedAt
) {
}