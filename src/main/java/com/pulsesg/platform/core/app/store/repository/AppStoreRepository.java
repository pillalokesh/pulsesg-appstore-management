package com.pulsesg.platform.core.app.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsesg.platform.core.app.store.entity.AppStoreEntity;

import java.util.List;

public interface AppStoreRepository extends JpaRepository<AppStoreEntity, Integer> {
    List<AppStoreEntity> findByAppType(String appType);
    // Additional query methods can be defined here if needed
}
