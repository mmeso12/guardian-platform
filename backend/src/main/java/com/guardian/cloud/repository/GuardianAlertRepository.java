package com.guardian.cloud.repository;

import com.guardian.cloud.entity.GuardianAlert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardianAlertRepository
        extends JpaRepository<GuardianAlert, Long> {

    boolean existsByDeviceEventId(Long deviceEventId);
}