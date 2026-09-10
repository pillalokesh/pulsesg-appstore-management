package com.pulsesg.platform.core.app.store.controller;

import com.pulsesg.platform.core.app.store.StoreConstants;
import com.pulsesg.platform.core.app.store.model.AppStore;
import com.pulsesg.platform.core.app.store.service.AppStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Profile("!practice")
@RequestMapping("/app-store")
public class AppStoreController {

    private final Logger log = LoggerFactory.getLogger(AppStoreController.class);

    @Autowired
    AppStoreService appStoreService;

    @GetMapping("/status")
    public String status() {
        return "Agent Store is running";
    }


    @GetMapping("/apps")
    public List<AppStore> getAllApps() {
         // This line is just to illustrate service usage; the result is not used.
        List<AppStore> apps = appStoreService.getAllApps();

        return apps;
       }

    @GetMapping("/user-all-apps")
    public List<AppStore> getAllAppsForUser(@RequestParam(name="userType",required = false) String userType,
                                            @RequestParam(name="storeType",required = true) String storeType,
                                            @RequestHeader(name=  StoreConstants.X_PULSE_SYS_GEN_USER_ID, required = false) String sysGeneratedUserId,
                                            @RequestHeader(name = StoreConstants.X_PULSE_TENANT_ID, required = true) String tenantId,
                                            @RequestHeader(name = StoreConstants.X_PULSE_ROLES, required = false) String roles
           ) {
        log.info("START :: Object ::" + "APP Store " + "||" +
                " OPERATION :: " + "get all user apps " + "||" +
                " USER ID :: " + sysGeneratedUserId + "||" +
                " TENANTID :: " + tenantId);

        return appStoreService.getAllAppsForUser(tenantId, sysGeneratedUserId, storeType, userType, roles);
    }

    @GetMapping("/user-active-apps")
    public List<AppStore> getActiveAppsForUser(@RequestParam(name="userType",required = false) String userType,
                                               @RequestParam(name="storeType",required = true) String storeType,
                                               @RequestHeader(name=  StoreConstants.X_PULSE_SYS_GEN_USER_ID, required = false) String sysGeneratedUserId,
                                               @RequestHeader(name = StoreConstants.X_PULSE_TENANT_ID, required = true) String tenantId,
                                               @RequestHeader(name = StoreConstants.X_PULSE_ROLES, required = false) String roles
           ){
        log.info("START :: Object ::" + "APP Store " + "||" +
                " OPERATION :: " + "get user active apps " + "||" +
                " USER ID :: " + sysGeneratedUserId + "||" +
                " TENANTID :: " + tenantId);

        return appStoreService.getActiveAppsForUser(tenantId, sysGeneratedUserId, true, storeType, userType, roles);
    }
}
