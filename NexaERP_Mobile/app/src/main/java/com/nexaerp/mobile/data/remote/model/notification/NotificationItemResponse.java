package com.nexaerp.mobile.data.remote.model.notification;

import java.time.LocalDateTime;

/**
 * Mirrors the backend's NotificationResponseDto. type/priority/module are kept as raw
 * strings (not Java enums) so the app never crashes on an enum value the backend adds later.
 */
public class NotificationItemResponse {
    private Long id;
    private String type;
    private String priority;
    private String module;
    private String title;
    private String message;
    private String route;
    private String entityType;
    private Long entityId;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public NotificationItemResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}