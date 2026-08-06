package com.nexaerp.mobile.data.remote.model.role;

import java.util.Set;

public class RoleResponse {
    private Long id;
    private String name;
    private String description;
    private Set<PermissionSummary> permissions;
    private Integer userCount;

    public RoleResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Set<PermissionSummary> getPermissions() { return permissions; }
    public void setPermissions(Set<PermissionSummary> permissions) { this.permissions = permissions; }
    public Integer getUserCount() { return userCount; }
    public void setUserCount(Integer userCount) { this.userCount = userCount; }

    public static class PermissionSummary {
        private Long id;
        private String code;
        private String name;
        private String module;

        public PermissionSummary() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getModule() { return module; }
        public void setModule(String module) { this.module = module; }
    }
}