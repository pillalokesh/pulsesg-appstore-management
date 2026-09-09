package com.pulsesg.platform.core.app.store.repository;

import com.pulsesg.platform.core.app.store.entity.UserAssignedAppsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAssignedAppsRepository extends JpaRepository<UserAssignedAppsEntity, Integer> {
    // Custom query methods can be added here if needed
    List<UserAssignedAppsEntity> findByUserIdAndActiveAndAppStoreEntity_Category(String userId, Boolean active, String category);

    List<UserAssignedAppsEntity> findByUserIdAndAppStoreEntity_Category(String userId, String category);

    List<UserAssignedAppsEntity> findByTenantIdAndAppStoreEntity_Category(String tenantId, String category);

    List<UserAssignedAppsEntity> findByTenantIdAndActiveAndAppStoreEntity_Category(String tenantId, Boolean active, String category);

    List<UserAssignedAppsEntity> findByTenantIdAndUserIdAndAppStoreEntity_Category(String tenantId, String userId, String storeType);

    List<UserAssignedAppsEntity> findByTenantIdAndAppStoreEntity_CategoryAndAppStoreEntity_AppType(String tenantId, String category, String appType);

    List<UserAssignedAppsEntity> findByTenantIdAndActiveAndAppStoreEntity_CategoryAndAppStoreEntity_AppType(String tenantId, Boolean active, String category, String appType);
}