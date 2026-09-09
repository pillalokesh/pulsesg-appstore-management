package com.pulsesg.platform.core.app.store.entity;

import jakarta.persistence.GeneratedValue;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_assigned_apps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAssignedAppsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_store_id", nullable = false)
    private AppStoreEntity appStoreEntity;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "active", nullable = false)
    private Boolean active = false;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_on", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdOn;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_on", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedOn;

    @Column(name = "sort_order" , nullable = false)
    private Integer sortOrder;

    @Column(name = "roles", columnDefinition = "jsonb")
    private String roles;

}

