package com.pulsesg.platform.core.app.store.repository;

import com.pulsesg.platform.core.app.store.entity.UserAssignedAppsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAssignedAppsRepository extends JpaRepository<UserAssignedAppsEntity, Integer> {
    List<UserAssignedAppsEntity> findByUserIdAndActiveAndAppStoreEntity_AppTypeIgnoreCase(String userId, Boolean active, String appType);

    List<UserAssignedAppsEntity> findByUserIdAndAppStoreEntity_AppTypeIgnoreCase(String userId, String appType);

    List<UserAssignedAppsEntity> findByTenantIdAndAppStoreEntity_AppTypeIgnoreCase(String tenantId, String appType);

    List<UserAssignedAppsEntity> findByTenantIdAndActiveAndAppStoreEntity_AppTypeIgnoreCase(String tenantId, Boolean active, String appType);

    List<UserAssignedAppsEntity> findByTenantIdAndUserIdAndAppStoreEntity_AppTypeIgnoreCase(String tenantId, String userId, String appType);

    List<UserAssignedAppsEntity> findByTenantIdAndAppStoreEntity_CategoryAndAppStoreEntity_AppTypeIgnoreCase(String tenantId, String category, String appType);

    List<UserAssignedAppsEntity> findByTenantIdAndActiveAndAppStoreEntity_CategoryAndAppStoreEntity_AppTypeIgnoreCase(String tenantId, Boolean active, String category, String appType);
}