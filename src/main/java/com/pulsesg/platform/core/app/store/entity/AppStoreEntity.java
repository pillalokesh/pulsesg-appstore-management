package com.pulsesg.platform.core.app.store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_store")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppStoreEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "externalAttributes", columnDefinition = "jsonb")
    private String externalAttributes;

    @Column(name = "owner", length = 100)
    private String owner;

    @Column(name = "link")
    private String link;

    @Column(name = "icon")
    private String icon;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_on", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdOn;

    @Column(name = "version")
    private String version;

    @Column(name = "visible_image_url")
    private String visibleImageUrl;

    @Column(name = "invisible_image_url")
    private String invisibleImageUrl;

    @Column(name = "app_type")
    private String appType;

}
