package com.pulsesg.platform.core.app.store.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("practice")
public class PracticeController {

    @GetMapping({"/", "/status"})
    public String status() {
        return "App Store practice deployment is running";
    }
}
