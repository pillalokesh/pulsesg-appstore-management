package com.pulsesg.platform.core.app.store.model;

import com.google.cloud.storage.Blob;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppStore {

    private Integer id;
    private String name;
    private String description;
    private String category;
    private String status;
    private String externalAttributes;
    private String owner;
    private String link;
    private String icon;
    private String createdBy;
    private LocalDateTime createdOn;
    private String version;
    private String visibleImageUrl;
    private String invisibleImageUrl;
    private Integer sortOrder;
}

