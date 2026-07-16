package com.guardian.cloud.repository;

import com.guardian.cloud.entity.LocationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRecordRepository
        extends JpaRepository<LocationRecord, Long> {

    Optional<LocationRecord>
    findTopByDeviceIdOrderByRecordedAtDesc(Long deviceId);

    List<LocationRecord>
    findTop100ByDeviceIdOrderByRecordedAtDesc(Long deviceId);

    boolean existsByDeviceIdAndSequenceNumber(
            Long deviceId,
            Long sequenceNumber
    );
}