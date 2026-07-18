package com.guardian.cloud.repository;

import com.guardian.cloud.entity.GuardianUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuardianUserRepository
        extends JpaRepository<GuardianUser, Long> {

    Optional<GuardianUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}