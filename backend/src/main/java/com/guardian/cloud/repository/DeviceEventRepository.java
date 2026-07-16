package com.guardian.cloud.repository;

import com.guardian.cloud.entity.AlertSeverity;
import com.guardian.cloud.entity.DeviceEvent;
import com.guardian.cloud.entity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceEventRepository
        extends JpaRepository<DeviceEvent, Long> {

    List<DeviceEvent> findTop100ByDeviceIdOrderByRecordedAtDesc(
            Long deviceId
    );

    List<DeviceEvent> findAllByDeviceIdAndEventTypeOrderByRecordedAtDesc(
            Long deviceId,
            EventType eventType
    );

    List<DeviceEvent> findAllBySeverityAndResolvedAtIsNullOrderByRecordedAtDesc(
            AlertSeverity severity
    );

    boolean existsByDeviceIdAndSequenceNumber(
            Long deviceId,
            Long sequenceNumber
    );
}