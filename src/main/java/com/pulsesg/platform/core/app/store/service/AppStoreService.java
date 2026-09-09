package com.pulsesg.platform.core.app.store.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsesg.platform.core.app.store.entity.AppStoreEntity;
import com.pulsesg.platform.core.app.store.model.AppStore;
import com.pulsesg.platform.core.app.store.repository.AppStoreRepository;
import com.pulsesg.platform.core.app.store.repository.UserAssignedAppsRepository;
import com.pulsesg.platform.core.app.store.entity.UserAssignedAppsEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppStoreService {

    private final Logger log = LoggerFactory.getLogger(AppStoreService.class);

    @Autowired
    AppStoreRepository appStoreRepository;

    @Autowired
    UserAssignedAppsRepository userAssignedAppsRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

   /* @Value("${http.pulsesg.main}")
    private String appStoreHost;*/

    public List<AppStore> getAllApps() {
        List<AppStoreEntity> appStores = appStoreRepository.findAll();
        List<AppStore> appStoresList = new ArrayList<>();
        buildAppStore(appStores, appStoresList);
        return appStoresList;
    }

    private void buildAppStore(List<AppStoreEntity> appStores, List<AppStore> appStoresList) {
        for (AppStoreEntity entity : appStores) {
            appStoresList.add(buildAppStore(entity, null));
        }
    }

    private AppStore buildAppStore(AppStoreEntity entity, Integer sortOrder) {
        AppStore app = new AppStore();
        app.setId(entity.getId());
        app.setName(entity.getName());
        app.setDescription(entity.getDescription());
        app.setCategory(entity.getCategory());
        app.setStatus(entity.getStatus());
        app.setExternalAttributes(entity.getExternalAttributes());
        app.setOwner(entity.getOwner());
        buildLink(entity, app);
        app.setIcon(entity.getIcon());
        app.setCreatedBy(entity.getCreatedBy());
        app.setCreatedOn(entity.getCreatedOn());
        app.setVersion(entity.getVersion());
        app.setVisibleImageUrl(entity.getVisibleImageUrl());
        app.setInvisibleImageUrl(entity.getInvisibleImageUrl());
        app.setSortOrder(sortOrder);
        return app;
    }

    private void buildLink(AppStoreEntity entity, AppStore app) {
      /*  if(StringUtils.isNotEmpty(entity.getLink()) && entity.getLink().startsWith("/") &&
            entity.getName().equalsIgnoreCase("Copilot")) {
            app.setLink("https://ai."+appStoreHost+ entity.getLink());
        } else {*/
            app.setLink(entity.getLink());
        //}
    }

    public Optional<AppStoreEntity> getAppById(Integer id) {
        return appStoreRepository.findById(id);
    }

    public AppStoreEntity saveApp(AppStoreEntity appStoreEntity) {
        return appStoreRepository.save(appStoreEntity);
    }

    public void deleteApp(Integer id) {

        appStoreRepository.deleteById(id);
    }

    public List<AppStore> getAllAppsForUser(String tenantId, String userId, String storeType, String userType, String roles) {
        log.info("START :: getAllAppsForUser :: USER ID :: " + userId);
        List<UserAssignedAppsEntity> assignedApps = new ArrayList<>();
        if (StringUtils.isNotEmpty(userType) && userType.equalsIgnoreCase("self-registered")) {
            assignedApps = userAssignedAppsRepository.findByTenantIdAndAppStoreEntity_Category("public", storeType);
        } else if (StringUtils.isNotEmpty(tenantId)) {
            assignedApps = userAssignedAppsRepository.findByTenantIdAndAppStoreEntity_Category(tenantId, storeType);
        } else if (StringUtils.isNotEmpty(userId)) {
            assignedApps = userAssignedAppsRepository.findByUserIdAndAppStoreEntity_Category(userId, storeType);
        }
        List<AppStore> appStoresList = new ArrayList<>();
        Set<String> userRoles = parseRoles(roles);
        for (UserAssignedAppsEntity assigned : assignedApps) {
            AppStoreEntity entity = assigned.getAppStoreEntity();
            if (entity != null && hasRoleAccess(assigned.getRoles(), userRoles)) {
                appStoresList.add(buildAppStore(entity, assigned.getSortOrder()));
            }
        }
        appStoresList.sort((a, b) -> a.getSortOrder().compareTo(b.getSortOrder()));
        log.info("END :: getAllAppsForUser :: USER ID :: " + userId + " appStoresList Size :: " + appStoresList.size());
        return appStoresList;
    }

    public List<AppStore> getActiveAppsForUser(String tenantId, String userId, boolean active, String storeType, String userType, String roles) {
        log.info("START :: getActiveAppsForUser :: USER ID :: " + userId);
        List<UserAssignedAppsEntity> assignedApps = new ArrayList<>();
        if (StringUtils.isNotEmpty(userType) && userType.equalsIgnoreCase("self-registered")) {
            assignedApps = userAssignedAppsRepository.findByTenantIdAndActiveAndAppStoreEntity_Category("public", true, storeType);
        } else if (StringUtils.isNotEmpty(tenantId)) {
            assignedApps = userAssignedAppsRepository.findByTenantIdAndActiveAndAppStoreEntity_Category(tenantId, active, storeType);
        } else if (StringUtils.isNotEmpty(userId)) {
            assignedApps = userAssignedAppsRepository.findByUserIdAndActiveAndAppStoreEntity_Category(userId, active, storeType);
        }
        List<AppStore> appStoresList = new ArrayList<>();
        Set<String> userRoles = parseRoles(roles);
        for (UserAssignedAppsEntity assigned : assignedApps) {
            AppStoreEntity entity = assigned.getAppStoreEntity();
            if (entity != null && hasRoleAccess(assigned.getRoles(), userRoles)) {
                appStoresList.add(buildAppStore(entity, assigned.getSortOrder()));
            }
        }
        appStoresList.sort((a, b) -> a.getSortOrder().compareTo(b.getSortOrder()));
        log.info("END :: getActiveAppsForUser :: USER ID :: " + userId + " appStoresList Size :: " + appStoresList.size());
        return appStoresList;
    }

    // Returns true if user has access based on role — if app has no roles configured, access is open to all
    private boolean hasRoleAccess(String appRolesJson, Set<String> userRoles) {
        if (StringUtils.isEmpty(appRolesJson) || userRoles.isEmpty()) {
            return true;
        }
        try {
            List<JsonNode> appRoles = objectMapper.readValue(appRolesJson, new TypeReference<List<JsonNode>>() {});
            if (appRoles.isEmpty()) return true;
            return appRoles.stream()
                    .map(node -> node.path("roleName").asText(null))
                    .filter(Objects::nonNull)
                    .anyMatch(userRoles::contains);
        } catch (Exception e) {
            log.warn("Failed to parse app roles JSON: {}", appRolesJson);
            return true;
        }
    }

    // Parses the X-PULSE-ROLES header value (JSON array from gateway) into a Set
    private Set<String> parseRoles(String rolesHeader) {
        if (StringUtils.isEmpty(rolesHeader)) {
            return Collections.emptySet();
        }
        try {
            List<String> roleList = objectMapper.readValue(rolesHeader, new TypeReference<List<String>>() {});
            return roleList.stream().collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("Failed to parse roles header: {}", rolesHeader);
            return Collections.emptySet();
        }
    }
}
