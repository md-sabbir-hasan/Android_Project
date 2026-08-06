package com.nexaerp.mobile.data.remote.model.role;

import java.util.Set;

public class RoleRequest {
    private String name;
    private String description;
    private Set<Long> permissionIds;

    public RoleRequest() {}

    public RoleRequest(String name, String description, Set<Long> permissionIds) {
        this.name = name;
        this.description = description;
        this.permissionIds = permissionIds;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Set<Long> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(Set<Long> permissionIds) { this.permissionIds = permissionIds; }
}