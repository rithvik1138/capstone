package com.cloudmonitoring.repository;

import com.cloudmonitoring.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByDeviceId(Long deviceId);

    Optional<DeviceToken> findByTokenHash(String tokenHash);

    Optional<DeviceToken> findByTokenHashAndIsRevokedFalse(String tokenHash);

    void deleteByDeviceId(Long deviceId);
}