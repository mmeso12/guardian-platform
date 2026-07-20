package com.guardian.cloud.repository;

import com.guardian.cloud.entity.LocationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LocationRecordRepository
        extends JpaRepository<LocationRecord, Long> {

    Optional<LocationRecord>
    findTopByDeviceIdOrderByRecordedAtDesc(Long deviceId);

    List<LocationRecord>
    findTop100ByDeviceIdOrderByRecordedAtDesc(Long deviceId);

    List<LocationRecord> findAllByDeviceIdAndRecordedAtBetweenOrderByRecordedAtDesc(
        Long deviceId,
        Instant from,
        Instant to
);

    boolean existsByDeviceIdAndSequenceNumber(
            Long deviceId,
            Long sequenceNumber
    );
}